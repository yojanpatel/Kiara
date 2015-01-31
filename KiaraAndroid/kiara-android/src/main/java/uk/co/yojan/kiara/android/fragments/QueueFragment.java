package uk.co.yojan.kiara.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.adapters.SongListViewAdapter;
import uk.co.yojan.kiara.android.comparators.SongComparatorByArtist;
import uk.co.yojan.kiara.android.events.QueueSongRequest;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.client.data.Song;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A placeholder fragment containing a simple view.
 */
public class QueueFragment extends KiaraFragment {

  private static final String log = QueueFragment.class.getName();

  private Context mContext;
  private KiaraActivity activity;

  private Long playlistId;

  @InjectView(R.id.queue_recycler_view) RecyclerView mRecyclerView;
  private SongListViewAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private ArrayList<SongParcelable> songs;
  private ArrayList<SongParcelable> displaySongs;

  private EditText searchEdit;
  private ImageButton resetQueryButton;

  public QueueFragment() {
    // Required empty public constructor
  }

 /**
 * Factory method to create the fragment from the activity.
 * It allows easier transfer of data.
 */
  public static QueueFragment newInstance(long id, ArrayList<SongParcelable> songs) {
    QueueFragment fragment =  new QueueFragment();
    Bundle args = new Bundle();
    if(songs != null && !songs.isEmpty()) {
      args.putParcelableArrayList(Constants.ARG_PLAYLIST_SONG_LIST, songs);
      fragment.setArguments(args);
    }
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.activity = (KiaraActivity) getActivity();

    if (getArguments() != null) {
      // The values passed from the playlist with songs.
      songs = getArguments().getParcelableArrayList(Constants.ARG_PLAYLIST_SONG_LIST);
      Collections.sort(songs, new SongComparatorByArtist());
      displaySongs = SongParcelable.clone(songs);
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
    View rootView = inflater.inflate(R.layout.fragment_queue, container, false);
    ButterKnife.inject(this, rootView);
    this.mContext = rootView.getContext();

    // Use previous set of songs (the ones cached already) until the network call is returned.
    this.mAdapter = new SongListViewAdapter(displaySongs, mContext);

    mRecyclerView.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(mContext,
        new RecyclerItemTouchListener.OnItemClickListener() {
          @Override
          public void onItemClick(View view, int position) {
            SongParcelable queued = mAdapter.getData().get(position);
            Log.d(log, "Queueing " + queued.getSongName());
            getKiaraActivity().toast(queued.getArtistName() + " - " + queued.getSongName() + " queued.", true);
            getBus().post(new QueueSongRequest(queued));
          }
        }));

    searchEdit.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {}

      @Override
      public void afterTextChanged(Editable s) {

        if(s.length() > 0)
          resetQueryButton.setVisibility(View.VISIBLE);
        else
          resetQueryButton.setVisibility(View.GONE);

        displaySongs = filterSongs(s.toString());
        mAdapter.updateSongs(displaySongs);
      }
    });

    return rootView;
  }

  private ArrayList<SongParcelable> filterSongs(String query) {
    ArrayList<SongParcelable> ret = new ArrayList<SongParcelable>();
    for(SongParcelable sp : songs) {
      String songText = sp.getSongName() + sp.getArtistName() + sp.getAlbumName();
      songText = songText.toLowerCase();

      if((songText).contains(query.toLowerCase())) {
        ret.add(sp);
      }
    }
    return ret;
  }

  @Subscribe
  public void updateSongs(ArrayList<Song> songs) {
//    this.songs = convert(songs);

    if(songs.size() > 0 && songs.get(0) != null) {
      Log.d(log, "onSongsReceived, updating the list.");
      for (Song s : songs) {
        SongParcelable sp = new SongParcelable(s);
        if(!this.songs.contains(sp)) {
          this.songs.add(new SongParcelable(s));
        }
      }

      Collections.sort(this.songs, new SongComparatorByArtist());
      displaySongs = SongParcelable.clone(this.songs);

      mAdapter.notifyDataSetChanged();
      activity.setProgressBarVisibility(View.GONE);
    }
  }
}