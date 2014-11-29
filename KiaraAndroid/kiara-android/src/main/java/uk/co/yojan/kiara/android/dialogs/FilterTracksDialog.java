package uk.co.yojan.kiara.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.adapters.FilterTracksAdapter;
import uk.co.yojan.kiara.client.data.spotify.PlaylistTracks;

/**
 * Filter Tracks Dialog - a dialog to display all tracks for a playlist.
 *  The user should be able to swipe left/right to delete certain songs before
 *  adding to the given playlist.
 */
public class FilterTracksDialog extends DialogFragment {

  Context mContext;
  KiaraActivity activity;

  @InjectView(R.id.tracks_list) RecyclerView tracksList;
  @InjectView(R.id.progressBar) ProgressBar progressBar;

  private String playlistId;
  private PlaylistTracks tracks;

  private FilterTracksAdapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;


  public static FilterTracksDialog newInstance(String spotifyPlaylistId) {
    FilterTracksDialog ftd = new FilterTracksDialog();
    ftd.setPlaylistId(spotifyPlaylistId);
    return ftd;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    this.activity = (KiaraActivity)getActivity();
    this.mContext = getActivity().getApplicationContext();

    View view = activity.getLayoutInflater().inflate(R.layout.filter_song_dialog, null);
    ButterKnife.inject(this, view);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setView(view)
        .setPositiveButton(R.string.add_all, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // TODO(yojan) batch add event.
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            // User cancelled the dialog, do nothing.
            dialogInterface.cancel();
          }
        });

    tracksList.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(activity);
    tracksList.setLayoutManager(mLayoutManager);
    return builder.create();
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

  @Subscribe
  public void onPlaylistTracksReceived(final PlaylistTracks tracks) {
    Log.d("FilterTracksDialog", "Received PlaylistTracks consisting of " + tracks.getTracks().size() + " tracks.");
    progressBar.setVisibility(View.INVISIBLE);
    tracksList.setVisibility(View.VISIBLE);

    this.tracks = tracks;
    this.mAdapter = new FilterTracksAdapter(tracks, mContext);
    tracksList.setAdapter(mAdapter);
  }

  public void setPlaylistId(String playlistId) {
    this.playlistId = playlistId;
  }
}
