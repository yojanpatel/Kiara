package uk.co.yojan.kiara.android.events;


public class AddSong {

  private long playlistId;
  private String songId;

  public AddSong(long playlistId, String songId) {
    this.playlistId = playlistId;
    this.songId = songId;
  }

  public long getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(long playlistId) {
    this.playlistId = playlistId;
  }

  public String getSongId() {
    return songId;
  }

  public void setSongId(String songId) {
    this.songId = songId;
  }
}
