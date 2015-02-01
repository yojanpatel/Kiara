package uk.co.yojan.kiara.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.faradaj.blurbehind.BlurBehind;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.BrowseActivity;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.adapters.SpotifyPlaylistAdapter;
import uk.co.yojan.kiara.android.events.GetSpotifyPlaylistsForUser;
import uk.co.yojan.kiara.android.events.SpotifyPlaylists;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.client.data.spotify.Playlist;

import java.util.ArrayList;

/**
 * Created by yojan on 2/1/15.
 */
public class SpotifyPlaylistFragment extends KiaraFragment {

  private static final String log = SpotifyPlaylistFragment.class.getName();

  private KiaraActivity parent;
  private Context mContext;

  @InjectView(R.id.playlist_recycler_view) RecyclerView mRecyclerView;
  @InjectView(R.id.progressBar) ProgressBar progressBar;
  private SpotifyPlaylistAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  private ArrayList<Playlist> playlists;
  private long playlistId;

  private int width;
  private int height;

  public static SpotifyPlaylistFragment newInstance(long playlistId) {
    SpotifyPlaylistFragment fragment = new SpotifyPlaylistFragment();
    Bundle args = new Bundle();
    args.putLong(Constants.ARG_PLAYLIST_ID, playlistId);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Get screen size for resizing
    WindowManager wm = (WindowManager) getKiaraActivity().getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    width = size.x;
    height = size.y;

    Log.d(log, "onCreate");
    Bundle args = getArguments();
    if (args != null) {
      playlistId = args.getLong(Constants.ARG_PLAYLIST_ID);
    }
  }



  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_spotify_playlist, container, false);

    ButterKnife.inject(this, rootView);
    this.mContext = rootView.getContext();

    mRecyclerView.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(getKiaraActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new SpotifyPlaylistAdapter(mContext, width, height);
    mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(mContext, new RecyclerItemTouchListener.OnItemClickListener() {
      @Override
      public void onItemClick(View view, final int position) {
        Runnable run = new Runnable() {
          @Override
          public void run() {

            Playlist playlist = playlists.get(position);

            Intent intent = new Intent(getKiaraActivity(), BrowseActivity.class);
            intent.putExtra(Constants.ARG_PLAYLIST_SPOTIFY_ID, playlist.getUri());
            intent.putExtra(Constants.ARG_PLAYLIST_SPOTIFY_NAME, playlist.getName());
            intent.putExtra(Constants.ARG_PLAYLIST_ID, playlistId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
          }
        };
        BlurBehind.getInstance().withAlpha(50).execute(getKiaraActivity(), run);
      }
    }));
    mRecyclerView.setAdapter(mAdapter);

    parent = (KiaraActivity) getActivity();
    return rootView;
  }


  @Override
  public void onResume() {
    Log.d(log, "onResume");
    super.onResume();
    getBus().post(new GetSpotifyPlaylistsForUser(parent.getUserId()));

  }


  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
  }

  @Subscribe
  public void onPlaylistsReceived(SpotifyPlaylists playlists) {
    progressBar.setVisibility(View.INVISIBLE);
    ArrayList<Playlist> ps = playlists.getPlaylists();
    this.playlists = ps;
    mAdapter.updateList(ps);
  }
}
