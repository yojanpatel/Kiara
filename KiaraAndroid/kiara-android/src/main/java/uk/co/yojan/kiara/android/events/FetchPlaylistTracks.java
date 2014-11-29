package uk.co.yojan.kiara.android.events;


public class FetchPlaylistTracks {

  String userId;
  String playlistId;

  public FetchPlaylistTracks(String userId, String playlistId) {
    this.userId = userId;
    this.playlistId = playlistId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(String playlistId) {
    this.playlistId = playlistId;
  }
}
