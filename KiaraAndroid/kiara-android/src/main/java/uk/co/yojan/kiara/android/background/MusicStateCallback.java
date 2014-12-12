package uk.co.yojan.kiara.android.background;

import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

/**
 * Callback for Music State changes with the player object kept in MusicService.
 */
public interface MusicStateCallback {

  /**
   * Called everytime the Spotify stream is paused/resumed with the new status.
   * Any UI changes that need to be carried out can register themselves with MusicService
   * and this method will then be called.
   *
   * @param playing - current status of the spotify stream.
   */
  public void onPlayingStateChanged(boolean playing);

  /**
   * Called every time PlayerState is returned by the Native spotify SDK.
   *
   * @param state  PlayerState object consisting of various data on the current stream such
   *               as duration, current position etc.
   */
  public void onPlayerState(PlayerState state);

  public void onPlaybackEvent(PlayerNotificationCallback.EventType eventType, PlayerState playerState);

  }
