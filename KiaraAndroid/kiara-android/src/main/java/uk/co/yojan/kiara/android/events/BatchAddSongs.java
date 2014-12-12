package uk.co.yojan.kiara.android.events;

import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.ArrayList;

public class BatchAddSongs {
  private ArrayList<Track> tracks;
  private Long playlistId;

  public BatchAddSongs(ArrayList<Track> tracks, Long playlistId) {
    this.tracks = tracks;
    this.playlistId = playlistId;
  }

  public ArrayList<Track> getTracks() {
    return tracks;
  }

  public void setTracks(ArrayList<Track> tracks) {
    this.tracks = tracks;
  }

  public Long getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(Long playlistId) {
    this.playlistId = playlistId;
  }
}
