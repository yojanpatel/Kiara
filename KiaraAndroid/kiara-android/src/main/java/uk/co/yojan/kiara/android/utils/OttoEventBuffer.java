package uk.co.yojan.kiara.android.utils;

import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.events.AuthCodeGrantResponse;
import uk.co.yojan.kiara.android.events.CreatedPlaylist;
import uk.co.yojan.kiara.android.events.RefreshAccessTokenResponse;
import uk.co.yojan.kiara.android.events.SongAdded;
import uk.co.yojan.kiara.client.data.PlaylistWithSongs;
import uk.co.yojan.kiara.client.data.Song;
import uk.co.yojan.kiara.client.data.spotify.SpotifyUser;

import java.util.ArrayList;

/**
 * Class supporting catching and saving an HTTP responses temporarily
 */
public class OttoEventBuffer {

  private static final String log = OttoEventBuffer.class.getName();

  private Bus bus;
  private ArrayList savedMessages = new ArrayList();

  private boolean registeredToBus;


  public OttoEventBuffer(Bus eventBus) {
    bus = eventBus;
  }

  /** Starts saving any incoming responses or errors until stopped */
  public void startSaving() {
    Log.i(log, "Starting to buffer events.");
    if(!registeredToBus)
      bus.register(this);
    registeredToBus = true;
  }

  /** Sends out buffer and stops storing new */
  public void stopAndProcess() {
    Log.i(log, "Stopping to buffer events and posting the buffered ones.");
    unregister();
    for (Object message : savedMessages) {
      bus.post(message);
    }
    savedMessages.clear();
  }

  /** Clears buffers and stops storing new*/
  public void stopAndPurge() {
    if(registeredToBus)
      bus.unregister(this);
    savedMessages.clear();
  }

  private void unregister() {
    try {
      if(registeredToBus)
        bus.unregister(this);
      registeredToBus = false;
    } catch (Exception e) {
      Log.d("OttoEventBuffer", e.toString());
    }
  }


  /*
   * MAIN ACTIVITY
   */
  @Subscribe
  public void onAuthCodeGrantComplete(AuthCodeGrantResponse event) {
    Log.i(log, "Buffering AuthCodeGrantResponse");
    savedMessages.add(event);
  }

  @Subscribe
  public void onRefreshAccessComplete(RefreshAccessTokenResponse event) {
    Log.i(log, "Buffering RefreshAccessTokenResponse");
    savedMessages.add(event);
  }

  @Subscribe
  public void onCurrentUser(SpotifyUser user) {
    Log.i(log, "Buffering SpotifyUser");
    savedMessages.add(user);
  }

  /*
   * PlaylistListFragment.java
   */
  @Subscribe
  public void onNewPlaylistReceived(CreatedPlaylist event) {
    Log.i(log, "Buffering CreatedPlaylist");
    savedMessages.add(event);
  }

  @Subscribe
  public void onPlaylistsReceived(ArrayList<PlaylistWithSongs> songs) {
    Log.i(log, "Buffering PlaylistsReceived");
    savedMessages.add(songs);
  }

  /*
   * SongListFragment.java
   */
  @Subscribe
  public void onSongAdded(SongAdded event) {
    Log.i(log, "Buffering SongAdded");
    savedMessages.add(event);
  }

  @Subscribe
  public void onSongsReceived(ArrayList<Song> songs) {
    Log.i(log, "Buffering PlaylistsReceived");
    savedMessages.add(songs);
  }
}