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
import uk.co.yojan.kiara.android.KiaraApplication;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.activities.PlaylistSongListActivity;
import uk.co.yojan.kiara.android.adapters.PlaylistListViewAdapter;
import uk.co.yojan.kiara.android.client.VolleySingleton;
import uk.co.yojan.kiara.android.dialogs.CreatePlaylistDialog;
import uk.co.yojan.kiara.android.events.CreatedPlaylist;
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

  private KiaraActivity parent;
  private Context mContext;

  @InjectView(R.id.playlist_recycler_view) RecyclerView mRecyclerView;
  @InjectView(R.id.progressBar) ProgressBar progressBar;
  private PlaylistListViewAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  private ArrayList<PlaylistWithSongs> playlists;

  public PlaylistListFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_playlist_view, container, false);
    ButterKnife.inject(this, rootView);
    this.mContext = rootView.getContext();

    mRecyclerView.setHasFixedSize(true);

    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);

    mAdapter = new PlaylistListViewAdapter(mContext);
    mRecyclerView.setAdapter(mAdapter);

    mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(mContext,new RecyclerItemTouchListener.OnItemClickListener() {
      @Override
      public void onItemClick(View view, int position) {
        Log.d(log, "Item clicked at position " + position);
        long playlistId = playlists.get(position).getPlaylist().getId();

        /* Look for cached result for the playlist's songs. */
        List<Song> songs = getKiaraApplication().kiaraClient().getCachedSongs("yojanpatel", playlistId);
        if(songs == null) {
          songs = playlists.get(position).getSongs();
        }

        Intent i = new Intent(mContext, PlaylistSongListActivity.class);
        i.putExtra(PlaylistSongListActivity.SONG_LIST_ARG_KEY, SongParcelable.convert(songs));
        i.putExtra(PlaylistSongListActivity.PLAYLIST_ID_ARG_KEY, playlistId);
        getBus().post(new GetSongsForPlaylist(playlistId));
        startActivity(i);
      }
    }));

    parent = (KiaraActivity) getActivity();
    parent.getBus().post(new GetAllPlaylists());

    return rootView;
  }

  @Subscribe
  public void onPlaylistsReceived(final ArrayList<PlaylistWithSongs> rcvd) {
    if(rcvd.size() > 0 && rcvd.get(0) instanceof  PlaylistWithSongs) {
      Log.d(log, "Playlists received. " + rcvd.size());
      mAdapter.updateList(rcvd);
      playlists = rcvd;
      progressBar.setVisibility(View.INVISIBLE);
      setUpFab();
    }
  }

  @Subscribe
  public void onNewPlaylistCreated(final CreatedPlaylist cp) {
    Crouton.makeText(parent, "New Playlist! " + cp.getPlaylist().getPlaylistName(), Style.CONFIRM);
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
}
