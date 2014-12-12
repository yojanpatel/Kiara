package uk.co.yojan.kiara.android.fragments;


import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.BrowseActivity;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.activities.PlaylistSongListActivity;
import uk.co.yojan.kiara.android.adapters.PlaylistListViewAdapter;
import uk.co.yojan.kiara.android.dialogs.CreatePlaylistDialog;
import uk.co.yojan.kiara.android.events.CreatedPlaylist;
import uk.co.yojan.kiara.android.events.FetchPlaylistTracks;
import uk.co.yojan.kiara.android.events.GetAllPlaylists;
import uk.co.yojan.kiara.android.events.GetSongsForPlaylist;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.android.views.FloatingActionButton;
import uk.co.yojan.kiara.client.data.PlaylistWithSongs;
import uk.co.yojan.kiara.client.data.Song;

import java.util.ArrayList;
import java.util.List;


public class PlaylistListFragment extends KiaraFragment {

  private static final String log = PlaylistListFragment.class.getName();

  public static final String CASE_PARAM = "CASE_PARAM";
  public static final String USER_PARAM = "USER_PARAM";
  public static final String PLAYLIST_PARAM = "PLAYLIST_PARAM";
  public static final String PLAYLIST_ID_PARAM = "PLAYLIST_ID_PARAM";
  public static final String PLAYLIST_NAME_PARAM = "PLAYLIST_NAME_PARAM";
  public static final String ALBUM_PARAM = "ALBUM_PARAM";
  public static final String TRACK_PARAM = "TRACK_PARAM";

  private KiaraActivity parent;
  private Context mContext;

  @InjectView(R.id.playlist_recycler_view) RecyclerView mRecyclerView;
  @InjectView(R.id.progressBar) ProgressBar progressBar;
  private PlaylistListViewAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  private ArrayList<PlaylistWithSongs> playlists;

  private Constants.Case c;
  private String userId;
  private String playlistId;
  private String playlistName;
  private String albumId;
  private String trackId;

  /*
   * Construct a PlaylistListFragment to allow user to choose which playlist
   * to add the media to. A dialog is then brought up for playlists/albums so
   * they can then remove some songs from the collection.
   */
  public static PlaylistListFragment newInstance(Bundle args) {
    PlaylistListFragment fragment = new PlaylistListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d("PVA", "onCreate");
    Bundle args = getArguments();
    if (args != null) {
      c = (Constants.Case)args.getSerializable(CASE_PARAM);
      switch(c) {
        case Playlist:
          userId = args.getString(USER_PARAM);
          playlistName = args.getString(PLAYLIST_NAME_PARAM);
          playlistId = args.getString(PLAYLIST_PARAM);
        case Track:
          trackId = args.getString(TRACK_PARAM);
        case Album:
          albumId = args.getString(ALBUM_PARAM);
      }
    } else {
      c = Constants.Case.Default;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_playlist_view, container, false);
    ButterKnife.inject(this, rootView);
    this.mContext = rootView.getContext();
    if(getArguments() != null)
      Log.d("PVA", "CASE: " + (Constants.Case)getArguments().getSerializable(CASE_PARAM));
    mRecyclerView.setHasFixedSize(true);

    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);

    mAdapter = new PlaylistListViewAdapter(mContext);
    mRecyclerView.setAdapter(mAdapter);
    Log.d("PVA", "onCreateView " + c);
    mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(mContext, new RecyclerItemTouchListener.OnItemClickListener() {
      @Override
      public void onItemClick(View view, int position) {
        if(c == Constants.Case.Default) {
          Log.d("PVA", "DEFAULT");
          defaultOnItemClick(view, position);
        } else if (c == Constants.Case.Playlist) {
          Log.d("PVA", "PLAYLIST");
          playlistOnItemClick(view, position);
        }
      }
    }));
    parent = (KiaraActivity) getActivity();
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    parent.getBus().post(new GetAllPlaylists());
    if(playlists == null)
      parent.addIndeterminateProgressBar();
  }

  @Subscribe
  public void onPlaylistsReceived(final ArrayList<PlaylistWithSongs> rcvd) {
    //noinspection ConstantConditions
    parent.setProgressBarVisibility(View.INVISIBLE);
    progressBar.setVisibility(View.INVISIBLE);
    if(rcvd.size() > 0 && rcvd.get(0) instanceof  PlaylistWithSongs) {
      if(playlists != null) Log.d(log, playlists.size() + "<" + rcvd.size());
      if(playlists == null || playlists.size() < rcvd.size()) {
        Log.d(log, "Playlists received. " + rcvd.size());
        mAdapter.updateList(rcvd);
        playlists = rcvd;
        if (c == Constants.Case.Default)
          setUpFab();
      }
    }
  }

  @Subscribe
  public void onNewPlaylistCreated(final CreatedPlaylist cp) {
    Crouton.makeText(parent, "New Playlist! " + cp.getPlaylist().getPlaylistName(), Style.CONFIRM);
    playlists.add(new PlaylistWithSongs(cp.getPlaylist(), null));
    mAdapter.addPlaylist(cp.getPlaylist());
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
  }

  private void setUpFab() {
    Drawable plus = getResources().getDrawable(R.drawable.ic_add_white_24dp);
    FloatingActionButton fab = new FloatingActionButton.Builder(getActivity())
        .withButtonColor(getResources().getColor(R.color.pinkA200))
        .withDrawable(plus)
        .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
        .withMargins(0, 0, 24, 24)
        .create();

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        FragmentManager fm = getFragmentManager();
        new CreatePlaylistDialog().show(fm, "fragment_create_playlist");
      }
    });
    fab.showFloatingActionButton();
  }


  public void defaultOnItemClick(View view, int position) {
    Log.d(log, "default - Item clicked at position " + position);
    long playlistId = playlists.get(position).getPlaylist().getId();

    Intent i = new Intent(mContext, PlaylistSongListActivity.class);

    /* Look for cached result for the playlist's songs. */
    String userId = ((KiaraActivity)getActivity()).sharedPreferences().getString(Constants.USER_ID, null);
    if(userId != null) {
      List<Song> songs = getKiaraApplication().kiaraClient().getCachedSongs(userId, playlistId);
      if (songs == null) {
        songs = playlists.get(position).getSongs();
      }
      i.putExtra(PlaylistSongListActivity.SONG_LIST_ARG_KEY, SongParcelable.convert(songs));
    }
    i.putExtra(PlaylistSongListActivity.PLAYLIST_ID_ARG_KEY, playlistId);
    i.putExtra(PlaylistSongListActivity.PLAYLIST_NAME_ARG_KEY, playlists.get(position).getPlaylist().getPlaylistName());
    getBus().post(new GetSongsForPlaylist(playlistId));
    startActivity(i);
  }


  public void playlistOnItemClick(View view, int position) {
    Log.d(log, "playlist - Item clicked at position " + position);

    // Post event to get all tracks for the playlist.
    getBus().post(new FetchPlaylistTracks(userId, playlistId));

    // Start the new Activity.
    FragmentManager fm = getFragmentManager();
    Intent intent = new Intent(mContext, BrowseActivity.class);
    intent.putExtra(CASE_PARAM, c);
    intent.putExtra(PLAYLIST_PARAM, playlistId);
    intent.putExtra(PLAYLIST_NAME_PARAM, playlistName);
    intent.putExtra(PLAYLIST_ID_PARAM, playlists.get(position).getPlaylist().getId());
    startActivity(intent);
  }
}
