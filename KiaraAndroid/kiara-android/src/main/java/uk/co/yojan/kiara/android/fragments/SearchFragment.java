package uk.co.yojan.kiara.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.adapters.SearchAdapter;
import uk.co.yojan.kiara.android.events.AddSong;
import uk.co.yojan.kiara.android.events.SearchRequest;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.client.data.spotify.*;

import java.util.*;


public class SearchFragment extends KiaraFragment {

  private long playlistId;

  private Context mContext;

  @InjectView(R.id.results_list) RecyclerView mRecyclerView;
  private RecyclerView.LayoutManager mLayoutManager;
  private SearchAdapter mAdapter;

  private EditText searchEdit;
  private ImageButton resetQueryButton;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   */
  public static SearchFragment newInstance(Long playlistId) {
    SearchFragment fragment = new SearchFragment();
    Bundle args = new Bundle();
    args.putLong(Constants.ARG_PLAYLIST_ID, playlistId);
    fragment.setArguments(args);
    return fragment;
  }

  public SearchFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      playlistId = getArguments().getLong(Constants.ARG_PLAYLIST_ID);
    }

    View toolbarLayout = getKiaraActivity().getToolbar().findViewById(R.id.toolbarLayout);
    searchEdit = (EditText) toolbarLayout.findViewById(R.id.searchEditText);
    resetQueryButton = (ImageButton) toolbarLayout.findViewById(R.id.resetQueryButton);

    resetQueryButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        searchEdit.setText("");
        resetQueryButton.setVisibility(View.GONE);
      }
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView =  inflater.inflate(R.layout.fragment_search, container, false);
    ButterKnife.inject(this, rootView);
    this.mContext = rootView.getContext();

    mRecyclerView.setHasFixedSize(false);

    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);


    // TODO: change
//    mAdapter = new SearchResultAdapter(new SearchResult(), mContext);
//    mRecyclerView.setAdapter(mAdapter);

    searchEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable s) {

        // Send search request
        if(s.length() > 0)
          resetQueryButton.setVisibility(View.VISIBLE);
        else
          resetQueryButton.setVisibility(View.GONE);


        // SEARCH SONG
//        displaySongs = filterSongs(s.toString());
        // ASYNC
        if(s.length() > 5)
          getKiaraActivity().getBus().post(new SearchRequest(s.toString(), playlistId, 0, 4));
      }
    });

    searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          Log.d("AddSongDialog", "search ime action rcvd.");
          searchClick();
          handled = true;
        }
        return handled;      }
    });

    return rootView;
  }

  public void searchClick() {
    String query = searchEdit.getText().toString();

//    Crouton.cancelAllCroutons();
//    searchCrouton = Crouton.makeText(getActivity(), "Searching for " + query, Style.INFO, (ViewGroup)getView());
//    searchCrouton.show();
//    progressBar.setVisibility(View.VISIBLE);
//    resultList.setVisibility(View.INVISIBLE);
    Log.d("AddSongDialog", getKiaraActivity().getKiaraApplication().spotifyWebService() == null ? "spotify web service null" : "not null");
    getKiaraActivity().getBus().post(new SearchRequest(query, playlistId, 0, 3));
  }

  private void search(String query) {

  }

  @Subscribe
  public void onSearchResultsReceived(final SearchResult result) {

    this.mAdapter = new SearchAdapter(mContext, result);
    mRecyclerView.setAdapter(mAdapter);

    mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(mContext,
      new RecyclerItemTouchListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, final int position) {
          List<Track> tracks = result.getTracks().getTracks();


          switch (mAdapter.getItemViewType(position)) {
            case SearchAdapter.VIEW_TRACK:
              Track track = tracks.get(position - 1);
              getKiaraActivity().getBus().post(new AddSong(playlistId, track.getId()));
              Crouton.makeText(getActivity(), "Adding " + track.getName(), Style.INFO, (ViewGroup) getView()).show();
              break;
            case SearchAdapter.VIEW_ARTIST:
              Artist a = mAdapter.artist(position);
              Map<String, String> options = new HashMap<String, String>();
              options.put("album_type", "album");
              options.put("market", getKiaraActivity().sharedPreferences().getString(Constants.USER_COUNTRY, "GB"));
              getKiaraApplication().spotifyApi().getArtistAlbums(a.getId(), options, new Callback<Pager<Album>>() {
                @Override
                public void success(Pager<Album> albumPager, Response response) {
                  mAdapter.addAlbums(position, removeDuplicates(albumPager.items));
                }

                @Override
                public void failure(RetrofitError error) {
                  Log.e("SearchAdapter", error.getMessage());
                }
              });
              break;
          }
        }
      }));
  }

  private List<Album> removeDuplicates(List<Album> albums) {
    HashSet<String> set = new HashSet<String>();
    for(Album a : albums) set.add(a.getName());

    ArrayList<Album> result = new ArrayList<Album>();
    for(Album a : albums) {
      if(set.contains(a.getName())) {
        result.add(a);
        set.remove(a.getName());
      }
    }
    return result;
  }
}
