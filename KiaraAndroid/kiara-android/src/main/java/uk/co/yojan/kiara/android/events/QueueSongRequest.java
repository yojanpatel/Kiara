package uk.co.yojan.kiara.android.events;

import uk.co.yojan.kiara.client.data.Song;

/**
 * Created by yojan on 12/19/14.
 */
public class QueueSongRequest {

  Song song;

  public QueueSongRequest(Song song) {
    this.song = song;
  }

  public Song getSong() {
    return song;
  }

  public void setSong(Song song) {
    this.song = song;
  }
}
