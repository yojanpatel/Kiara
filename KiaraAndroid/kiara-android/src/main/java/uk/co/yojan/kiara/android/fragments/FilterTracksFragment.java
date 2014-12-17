package uk.co.yojan.kiara.android.fragments;

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
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import org.apache.commons.lang.StringUtils;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.activities.KiaraActivity;
import uk.co.yojan.kiara.android.activities.PlaylistSongListActivity;
import uk.co.yojan.kiara.android.adapters.FilterTracksAdapter;
import uk.co.yojan.kiara.android.adapters.ParallaxRecyclerAdapter;
import uk.co.yojan.kiara.android.events.BatchAddSongs;
import uk.co.yojan.kiara.android.events.GetSongsForPlaylist;
import uk.co.yojan.kiara.android.listeners.RecyclerItemTouchListener;
import uk.co.yojan.kiara.android.listeners.SwipeDismissRecyclerViewTouchListener;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.android.views.FloatingActionButton;
import uk.co.yojan.kiara.android.views.FullImageView;
import uk.co.yojan.kiara.client.data.Song;
import uk.co.yojan.kiara.client.data.spotify.Playlist;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter Tracks Dialog - a dialog to display all tracks for a playlist.
 *  The user should be able to swipe left/right to delete certain songs before
 *  adding to the given playlist.
 */
public class FilterTracksFragment extends KiaraFragment {

  Context mContext;
  KiaraActivity activity;
  Picasso picasso;

  @InjectView(R.id.tracks_list) RecyclerView tracksList;
  @InjectView(R.id.progressBar) ProgressBar progressBar;

  private String spotifyPlaylistId;
  private String spotifyPlaylistName;
  private List<Track> tracks;

  private long playlistId;
  private String playlistName;

  private SwipeDismissRecyclerViewTouchListener touchListener;
  private ParallaxRecyclerAdapter<Track> parallaxAdapter;
  private RecyclerView.LayoutManager mLayoutManager;


  public static FilterTracksFragment newInstance(String spotifyPlaylistId, String playlistName) {
    FilterTracksFragment ftf = new FilterTracksFragment();
    ftf.setSpotifyPlaylistId(spotifyPlaylistId);
    ftf.setSpotifyPlaylistName(playlistName);
    return ftf;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    this.activity = (KiaraActivity)getActivity();

    View view = activity.getLayoutInflater().inflate(R.layout.filter_song_dialog, container, false);
    ButterKnife.inject(this, view);
    this.mContext = view.getContext();
    picasso = Picasso.with(mContext);
    picasso.setIndicatorsEnabled(false);
    initRecyclerView();
    return view;
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
    mLayoutManager = new LinearLayoutManager(activity);
    tracksList.setLayoutManager(mLayoutManager);
    touchListener = new SwipeDismissRecyclerViewTouchListener(tracksList,
        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {

          // Called to determine whether the given position can be dismissed.
          @Override
          public boolean canDismiss(int position) {
            return position != 0;
          }

          /* Called when the user has indicated they she would like to dismiss one or more list item
             positions. */
          @Override
          public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
            for(int viewPosition : reverseSortedPositions) {
              final int position = viewPosition - 1;
              final Track removedTrack = tracks.get(position);

              mLayoutManager.removeView(mLayoutManager.getChildAt(position));
              Log.d("FilterTracksFragment", "Swiped " + removedTrack.getName());
              tracks.remove(position);
              parallaxAdapter.notifyDataSetChanged();

              /* Display the Undo Snackbar */
              Snackbar.with(mContext).text(removedTrack.getName() + " removed.")
                  .actionLabel("Undo")
                  .actionColor(getResources().getColor(R.color.pinkA100))
                  .actionListener(new ActionClickListener() {
                    @Override
                    public void onActionClicked() {
                      // Undo - add the song back to the tracks list at the same position.
                      tracks.add(position, removedTrack);
                      parallaxAdapter.notifyDataSetChanged();
                    }
                  }).show(activity);
            }
          }
        });
    tracksList.setOnTouchListener(touchListener);

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
    parallaxAdapter.setOnParallaxScroll(new ParallaxRecyclerAdapter.OnParallaxScroll() {
      @Override
      public void onParallaxScroll(float percentage, float offset, View parallax) {
        // TODO(yojan): change toolbar opacity
      }
    });
    this.parallaxAdapter.setSwipeDismissRecyclerViewTouchListener(touchListener);
  }

  @Subscribe
  public void onPlaylistReceived(final Playlist playlist) {
    Log.d("FilterTracksDialog", "Received Playlist consisting of " + playlist.getTrackList().size() + " tracks.");
    progressBar.setVisibility(View.INVISIBLE);
    tracksList.setVisibility(View.VISIBLE);
    this.tracks = playlist.getTrackList();
    initParallax();
    tracksList.setAdapter(parallaxAdapter);

    View headerView = activity.getLayoutInflater().inflate(R.layout.header_image, null);
    parallaxAdapter.setParallaxHeader(headerView, tracksList);
    picasso.load(playlist.getImageUrl()).into((FullImageView)headerView.findViewById(R.id.header_image));
    ((TextView)headerView.findViewById(R.id.playlist_name)).setText(spotifyPlaylistName.toUpperCase());
    setUpFab();
  }

  private void setUpFab() {
    Drawable plus = getResources().getDrawable(R.drawable.ic_playlist_add_white_24dp);
    FloatingActionButton fab = new FloatingActionButton.Builder(getActivity())
        .withButtonColor(getResources().getColor(R.color.pinkA200))
        .withDrawable(plus)
        .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
        .withMargins(0, 0, 24, 24)
        .create();
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getBus().post(new BatchAddSongs(new ArrayList<Track>(tracks), playlistId));

        Intent i = new Intent(mContext, PlaylistSongListActivity.class);
        /* Look for cached result for the playlist's songs. */
        String userId = activity.sharedPreferences().getString(Constants.USER_ID, null);
        if(userId != null) {
          List<Song> songs = getKiaraApplication().kiaraClient().getCachedSongs(userId, playlistId);
          if (songs != null) {
            i.putExtra(Constants.ARG_PLAYLIST_SONG_LIST, SongParcelable.convert(songs));
          }
        }
        i.putExtra(Constants.ARG_PLAYLIST_ID, playlistId);
        i.putExtra(Constants.ARG_PLAYLIST_NAME, spotifyPlaylistName);
        getBus().post(new GetSongsForPlaylist(playlistId));
        startActivity(i);
      }
    });
    fab.showFloatingActionButton();
  }

  public void setSpotifyPlaylistId(String spotifyPlaylistId) {
    this.spotifyPlaylistId = spotifyPlaylistId;
  }

  public void setSpotifyPlaylistName(String name) {
    this.spotifyPlaylistName = name;
  }

  public long getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(long playlistId) {
    this.playlistId = playlistId;
  }

  public String getPlaylistName() {
    return playlistName;
  }

  public void setPlaylistName(String playlistName) {
    this.playlistName = playlistName;
  }
}
