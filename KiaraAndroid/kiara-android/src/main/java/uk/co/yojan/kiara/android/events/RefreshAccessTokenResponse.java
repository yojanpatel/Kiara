package uk.co.yojan.kiara.android.events;

public class RefreshAccessTokenResponse {
  public String accessToken;
  public int expiresIn;

  public RefreshAccessTokenResponse(String accessToken, int expiresIn) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
