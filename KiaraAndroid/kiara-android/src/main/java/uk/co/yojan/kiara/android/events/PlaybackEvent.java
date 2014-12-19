package uk.co.yojan.kiara.android.events;

import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

/**
 * Created by yojan on 12/19/14.
 */
public class PlaybackEvent {
  PlayerNotificationCallback.EventType event;
  PlayerState state;

  public PlaybackEvent(PlayerNotificationCallback.EventType event, PlayerState state) {
    this.event = event;
    this.state = state;
  }

  public PlayerNotificationCallback.EventType getEvent() {
    return event;
  }

  public void setEvent(PlayerNotificationCallback.EventType event) {
    this.event = event;
  }

  public PlayerState getState() {
    return state;
  }

  public void setState(PlayerState state) {
    this.state = state;
  }
}
