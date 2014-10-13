package uk.co.yojan.kiara.android.events;

public class RefreshAccessTokenRequest {

  private String refreshToken;

  public RefreshAccessTokenRequest(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
