package uk.co.yojan.kiara.android.events;

public class RefreshAccessTokenRequest {

  private String refreshToken;
  private String userId;

  public RefreshAccessTokenRequest(String refreshToken, String userId) {
    this.refreshToken = refreshToken;
    this.userId = userId;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
