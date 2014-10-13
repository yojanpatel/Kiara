package uk.co.yojan.kiara.android.events;

/**
 * Event for Authorization Code Grant Flow for Spotify.
 */
public class AuthCodeGrantRequestEvent {
  private String code;

  public AuthCodeGrantRequestEvent(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
