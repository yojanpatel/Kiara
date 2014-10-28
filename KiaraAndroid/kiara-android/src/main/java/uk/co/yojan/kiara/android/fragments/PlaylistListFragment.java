package uk.co.yojan.kiara.android.fragments;


import android.app.Fragment;
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
import butterknife.OnClick;
import butterknife.Optional;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.CreatePlaylistActivity;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.adapters.PlaylistListViewAdapter;
import uk.co.yojan.kiara.android.events.GetPlaylistsRequest;
import uk.co.yojan.kiara.android.views.FloatingActionButton;
import uk.co.yojan.kiara.client.data.Playlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PlaylistListFragment extends KiaraFragment {

  private static final String log = PlaylistListFragment.class.getName();

  private KiaraActivity parent;
  private Context mContext;

  @InjectView(R.id.playlist_recycler_view) RecyclerView mRecyclerView;
  @InjectView(R.id.progressBar) ProgressBar progressBar;
  private PlaylistListViewAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;

  private int fabId;

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
    parent = (KiaraActivity) getActivity();
    parent.getBus().post(new GetPlaylistsRequest());

    Drawable plus = getResources().getDrawable(android.R.drawable.ic_input_add);
    FloatingActionButton fab = new FloatingActionButton.Builder(getActivity())
        .withButtonColor(getResources().getColor(R.color.deeporange500))
        .withDrawable(plus)
        .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
        .withMargins(0, 0, 24, 24)
        .create();
    fab.setId(R.id.fab_playlist_create);

    return rootView;
  }

  @Subscribe
  public void onPlaylistsReceived(ArrayList<Playlist> rcvd) {
    Log.d(log, "playlists received. " + rcvd.size());
    List<Playlist> playlists = new ArrayList<Playlist>(rcvd);
    if(mAdapter == null) {
      mAdapter = new PlaylistListViewAdapter(playlists, mContext);
    } else {
      mAdapter.updateList(playlists);
    }
    mRecyclerView.setAdapter(mAdapter);
    progressBar.setVisibility(View.INVISIBLE);
  }

//  @Optional @OnClick(R.id.fab_playlist_create)
//  public void onPlaylistCreateClick(FloatingActionButton view) {
//    view.hideFloatingActionButton();
//    Intent i = new Intent(mContext, CreatePlaylistActivity.class);
//    startActivity(i);
//  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
  }
}
