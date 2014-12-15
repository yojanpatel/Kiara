package uk.co.yojan.kiara.client.data;

import java.util.ArrayList;
import java.util.List;

public class PlaylistWithSongs {

  Playlist playlist;
  List<Song> songs;

  public PlaylistWithSongs(Playlist playlist, List<Song> songs) {
    this.playlist = playlist;
    if(songs == null)
      this.songs = new ArrayList<Song>();
    else
      this.songs = songs;
  }

  public List<Song> getSongs() {
    return songs;
  }

  public void setSongs(List<Song> songs) {
    this.songs = songs;
  }

  public Playlist getPlaylist() {
    return playlist;
  }

  public void setPlaylist(Playlist playlist) {
    this.playlist = playlist;
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof Playlist)) return false;
    return playlist.getId().equals(((Playlist)obj).getId());
  }
}
