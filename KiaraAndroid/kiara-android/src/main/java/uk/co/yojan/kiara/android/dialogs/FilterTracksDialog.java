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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import org.apache.commons.lang.StringUtils;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.adapters.FilterTracksAdapter;
import uk.co.yojan.kiara.android.adapters.ParallaxRecyclerAdapter;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.android.listeners.SwipeDismissRecyclerViewTouchListener;
import uk.co.yojan.kiara.client.data.spotify.Playlist;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.List;

/**
 * Filter Tracks Dialog - a dialog to display all tracks for a playlist.
 *  The user should be able to swipe left/right to delete certain songs before
 *  adding to the given playlist.
 */
public class FilterTracksDialog extends DialogFragment {

  Context mContext;
  KiaraActivity activity;
  Picasso picasso;

  @InjectView(R.id.tracks_list) RecyclerView tracksList;
  @InjectView(R.id.progressBar) ProgressBar progressBar;

  private String playlistId;
  private String playlistName;
//  private PlaylistTracks tracks;
  private List<Track> tracks;

  private FilterTracksAdapter mAdapter;
  SwipeDismissRecyclerViewTouchListener touchListener;
  private ParallaxRecyclerAdapter<Track> parallaxAdapter;
  private RecyclerView.LayoutManager mLayoutManager;


  public static FilterTracksDialog newInstance(String spotifyPlaylistId, String playlistName) {
    FilterTracksDialog ftd = new FilterTracksDialog();
    ftd.setPlaylistId(spotifyPlaylistId);
    ftd.setPlaylistName(playlistName);
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
    picasso = Picasso.with(mContext);
    initRecyclerView();
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

  private void initRecyclerView() {
    tracksList.setHasFixedSize(true);
    mLayoutManager = new LinearLayoutManager(activity);
    tracksList.setLayoutManager(mLayoutManager);
    touchListener = new SwipeDismissRecyclerViewTouchListener(tracksList,
            new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {

              // Called to determine whether the given position can be dismissed.
              @Override
              public boolean canDismiss(int position) {
                return position != 0;
              }

              // Called when the user has indicated they she would like to dismiss one or more list item
              // positions.
              @Override
              public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                for(int viewPosition : reverseSortedPositions) {
                  int position = viewPosition - 1;
                  mLayoutManager.removeView(mLayoutManager.getChildAt(position));
                  Log.d("DIALOG SWIPE", "Removing " + tracks.get(position).getName());
                  tracks.remove(position);
                  mAdapter.notifyDataSetChanged();
                  parallaxAdapter.notifyDataSetChanged();
                }
              }
            });
    tracksList.setOnTouchListener(touchListener);

    // Set scroll listener so we don't look for swipes during scrolling.
    tracksList.setOnScrollListener(touchListener.makeScrollListener());
    tracksList.addOnItemTouchListener(new RecyclerItemTouchListener(mContext, new RecyclerItemTouchListener.OnItemClickListener() {
      @Override
      public void onItemClick(View view, int position) {
        int pos = position - 1;
        Log.d("DIALOG", tracks.get(pos).getId() + " " + tracks.get(pos).getName());

      }
    }));
  }

  private void initParallax() {
    this.parallaxAdapter = new ParallaxRecyclerAdapter<Track>(tracks);
    parallaxAdapter.implementRecyclerAdapterMethods(new ParallaxRecyclerAdapter.RecyclerAdapterMethods() {
      @Override
      public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        FilterTracksAdapter.ViewHolder viewHolder = (FilterTracksAdapter.ViewHolder) vh;
        Track track = tracks.get(position);

        viewHolder.title.setText(track.getName());

        StringBuilder sb = new StringBuilder();
        String artistName = track.getArtists().get(0).getName();
        String albumName = track.getAlbum().getName();
        sb.append(artistName);
        if (!StringUtils.isBlank(artistName) &&
            !StringUtils.isBlank(albumName)) {
          sb.append(" - ");
        }
        sb.append(albumName);
        viewHolder.detail.setText(sb.toString());
      }

      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.song_row_simple, parent, false);
        return new FilterTracksAdapter.ViewHolder(v);
      }

      @Override
      public int getItemCount() {
        return tracks.size();
      }
    });
    this.parallaxAdapter.setSwipeDismissRecyclerViewTouchListener(touchListener);
  }

  @Subscribe
  public void onPlaylistReceived(final Playlist playlist) {
    Log.d("FilterTracksDialog", "Received Playlist consisting of " + playlist.getTrackList().size() + " tracks.");
    progressBar.setVisibility(View.INVISIBLE);
    tracksList.setVisibility(View.VISIBLE);
//    playlistImg.setVisibility(View.VISIBLE);
//    picasso.load(playlist.getImageUrl()).into(playlistImg);
    this.tracks = playlist.getTrackList();
    this.mAdapter = new FilterTracksAdapter(tracks, mContext);
    initParallax();
    tracksList.setAdapter(parallaxAdapter);
//    tracksList.setAdapter(mAdapter);
  }

  public void setPlaylistId(String playlistId) {
    this.playlistId = playlistId;
  }

  public void setPlaylistName(String name) {
    this.playlistName = name;
  }


}
