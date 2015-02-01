package uk.co.yojan.kiara.android.events;


import uk.co.yojan.kiara.client.data.spotify.Playlist;

import java.util.ArrayList;

/**
 * Created by yojan on 2/1/15.
 */
public class SpotifyPlaylists {

  String userId;
  ArrayList<Playlist> playlists;

  public SpotifyPlaylists(String userId) {
    this.userId = userId;
    playlists = new ArrayList<Playlist>();
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public ArrayList<Playlist> getPlaylists() {
    return playlists;
  }

  public void setPlaylists(ArrayList<Playlist> playlists) {
    this.playlists = playlists;
  }

  public void addPlaylist(Playlist p) {
    this.playlists.add(p);
  }
}
