package uk.co.yojan.kiara.server.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import uk.co.yojan.kiara.server.serializers.AccessTokenSerializer;

@JsonSerialize(using = AccessTokenSerializer.class)
public class AccessToken {
  private String accessToken;
  private int expiresIn;

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public int getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }
}
