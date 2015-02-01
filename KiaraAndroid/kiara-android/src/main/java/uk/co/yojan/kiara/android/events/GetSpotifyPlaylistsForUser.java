package uk.co.yojan.kiara.android.events;

/**
 * Created by yojan on 2/1/15.
 */
public class GetSpotifyPlaylistsForUser {

  private String userId;

  public GetSpotifyPlaylistsForUser(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
