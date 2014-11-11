package uk.co.yojan.kiara.android.events;

import uk.co.yojan.kiara.client.data.Song;

public class SongAdded {

  private Song song;

  public SongAdded(Song song) {
    this.song = song;
  }

  public Song getSong() {
    return song;
  }

  public void setSong(Song song) {
    this.song = song;
  }
}
