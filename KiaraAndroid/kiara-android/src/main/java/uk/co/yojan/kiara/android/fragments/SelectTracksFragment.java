package uk.co.yojan.kiara.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.BrowseTabActivity;
import uk.co.yojan.kiara.android.activities.PlaylistSongListActivity;
import uk.co.yojan.kiara.android.adapters.SelectAdapter;
import uk.co.yojan.kiara.android.events.BatchAddSongs;
import uk.co.yojan.kiara.android.events.GetSongsForPlaylist;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class SelectTracksFragment extends DialogFragment {

  private Context mContext;
  private BrowseTabActivity activity;

  @InjectView(R.id.selectTracksList) RecyclerView selectTracksList;
  private SelectAdapter mAdapter;
  private LinearLayoutManager mLayoutManager;

  public static SelectTracksFragment newInstance() {
    SelectTracksFragment fragment = new SelectTracksFragment();
    return fragment;
  }

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public SelectTracksFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onResume() {
    super.onResume();
    activity.getBus().register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    activity.getBus().unregister(this);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    this.activity = (BrowseTabActivity)getActivity();
    this.mContext = getActivity().getApplicationContext();

    View view = activity.getLayoutInflater().inflate(R.layout.fragment_select_tracks, null);
    ButterKnife.inject(this, view);
    mLayoutManager = new LinearLayoutManager(activity);
    mAdapter = new SelectAdapter(activity);
    selectTracksList.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(activity);
    selectTracksList.setLayoutManager(mLayoutManager);
    selectTracksList.setAdapter(mAdapter);

    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setView(view);
    builder.setTitle("Add Songs");

    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.cancel();
      }
    });

    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {

        ArrayList<Track> tracks = new ArrayList<Track>();
        for(Track t : activity.selectedSongs()) {
          tracks.add(t);
        }
        activity.getBus().post(new BatchAddSongs(tracks, activity.playlistId()));
        activity.toast(tracks.size() + " songs added.");

        Intent i = new Intent(activity, PlaylistSongListActivity.class);
        i.putExtra(Constants.ARG_PLAYLIST_ID, activity.playlistId());
        i.putExtra(Constants.ARG_PLAYLIST_NAME, activity.playlistName());
        activity.getBus().post(new GetSongsForPlaylist(activity.playlistId()));
        startActivity(i);
        dialog.cancel();
        activity.finish();
      }
    });

    return builder.create();
  }



  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
  }
}
