package uk.co.yojan.kiara.server.models;

import java.util.List;

public class PlaylistWithSongs {

  Playlist playlist;
  List<Song> songs;

  public PlaylistWithSongs(Playlist playlist, List<Song> songs) {
    this.playlist = playlist;
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
}
