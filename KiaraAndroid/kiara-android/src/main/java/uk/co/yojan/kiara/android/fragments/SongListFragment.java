package uk.co.yojan.kiara.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.otto.Subscribe;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.BrowseTabActivity;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.activities.PlayerActivity;
import uk.co.yojan.kiara.android.adapters.SongListViewAdapter;
import uk.co.yojan.kiara.android.comparators.SongComparatorByArtist;
import uk.co.yojan.kiara.android.events.SongAdded;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.android.listeners.SwipeDismissRecyclerViewTouchListener;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.client.data.Song;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SongListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SongListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SongListFragment extends KiaraFragment {
  private static final String log = SongListFragment.class.getName();

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private SongListViewAdapter mAdapter;

  @InjectView(R.id.progressBar) ProgressBar progressBar;
  @InjectView(R.id.song_recycler_view) RecyclerView mRecyclerView;
  FloatingActionButton fab;

  private RecyclerView.LayoutManager mLayoutManager;
  private KiaraActivity activity;

  private ArrayList<SongParcelable> songs;
  private long playlistId;
  private String playlistName;

  /*
   * Factory method to create the fragment from the activity.
   * It allows easier transfer of data.
   */
  public static SongListFragment newInstance(long id, String name, ArrayList<SongParcelable> songs) {
    SongListFragment fragment =  new SongListFragment();
    Bundle args = new Bundle();
    args.putParcelableArrayList(Constants.ARG_PLAYLIST_SONG_LIST, songs);
    args.putLong(Constants.ARG_PLAYLIST_ID, id);
    args.putString(Constants.ARG_PLAYLIST_NAME, name);
    fragment.setArguments(args);
    return fragment;
  }

  public SongListFragment() {
      // Required empty public constructor
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      // The values passed from the playlist with songs.
      songs = getArguments().getParcelableArrayList(Constants.ARG_PLAYLIST_SONG_LIST);
      if(songs != null) {
        Collections.sort(songs, new SongComparatorByArtist());
      } else {
        songs = new ArrayList<SongParcelable>();
      }

      playlistId = getArguments().getLong(Constants.ARG_PLAYLIST_ID);
      playlistName = getArguments().getString(Constants.ARG_PLAYLIST_NAME);
      Log.d(log, playlistName);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_playlist_song_list, container, false);
    ButterKnife.inject(this, rootView);

    this.mContext = rootView.getContext();
    this.activity = (KiaraActivity) getActivity();
    ((TextView) activity.getToolbar().findViewById(R.id.toolbarTitle)).setText(playlistName);

    // Use previous set of songs (the ones cached already) until the network call is returned.
    this.mAdapter = new SongListViewAdapter(songs, mContext);
    Log.d(log, "Using previous set of songs until network call returned. ");

    initRecyclerView();
    progressBar.setVisibility(View.INVISIBLE);


    setUpFab();

    return rootView;
  }


  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
        mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
        throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  private void initRecyclerView() {
    mRecyclerView.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);
    mRecyclerView.setAdapter(mAdapter);

    SwipeDismissRecyclerViewTouchListener touchListener = new SwipeDismissRecyclerViewTouchListener(mRecyclerView,
        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
          @Override
          public boolean canDismiss(int position) {
            return true;
          }

          @Override
          public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
            Log.d(log, "onDismiss");
          }
        });
    mRecyclerView.setOnTouchListener(touchListener);
    mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());

    mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(mContext,
      new RecyclerItemTouchListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
          Log.d(log, "Item clicked at position " + position);
          Intent i = new Intent(mContext, PlayerActivity.class);

          ActivityOptionsCompat options =
              ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
//                    new Pair<View, String>(view.findViewById(R.id.song_img), getString(R.string.transition_album_cover)),
                  new Pair<View, String>(fab, getString(R.string.transition_fab)));

          i.putExtra(Constants.ARG_SONG, new SongParcelable(songs.get(position)));
          i.putExtra(Constants.ARG_PLAYLIST_ID, playlistId);
          ActivityCompat.startActivity(activity, i, options.toBundle());
        }
      }));
  }


  @Subscribe
  public void onSongAdded(final SongAdded song) {
    SongParcelable sp = new SongParcelable(song.getSong());
    if(!this.songs.contains(sp)) {
      Crouton.cancelAllCroutons();
      Crouton.makeText(activity, "Added song.", Style.CONFIRM).show();
      this.songs.add(sp);
      mAdapter.notifyDataSetChanged();
    }
  }

  /*
   * Update the RecyclerView with new data as the network call has returned.
   */
  @Subscribe
  public void onSongsReceived(final ArrayList<Song> songs) {

    if(songs.size() > 0 && songs.get(0) instanceof Song) {

      boolean initiallyEmpty = this.songs.isEmpty();

      Log.d(log, "onSongsReceived, updating the list.");
      for (Song s : songs) {
        SongParcelable sp = new SongParcelable(s);
        if(!this.songs.contains(sp)) {
          this.songs.add(new SongParcelable(s));
        }
      }

      if(this.songs.size() != songs.size()) {
        getKiaraActivity().toast(songs.size() + " songs added.");
      }

      Collections.sort(this.songs, new SongComparatorByArtist());
      mAdapter.notifyDataSetChanged();
      activity.setProgressBarVisibility(View.GONE);

    }
  }

  private void setUpFab() {
    Drawable plus = getResources().getDrawable(R.drawable.ic_add_white_24dp);
    fab = getKiaraActivity().getFab();

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
//        FragmentManager fm = getFragmentManager();
//        AddSongDialog asd = AddSongDialog.newInstance(playlistId);
//        asd.show(fm, "fragment_add_song");
//        Intent intent = new Intent(mContext, SearchActivity.class);
        Intent intent = new Intent(mContext, BrowseTabActivity.class);
        intent.putExtra(Constants.ARG_PLAYLIST_ID, playlistId);
        intent.putExtra(Constants.ARG_PLAYLIST_NAME, playlistName);
        startActivity(intent);
      }
    });
  }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
      // TODO: Update argument type and name
      public void onFragmentInteraction(Uri uri);
    }

}
