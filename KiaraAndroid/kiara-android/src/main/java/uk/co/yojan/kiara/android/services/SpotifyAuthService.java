package uk.co.yojan.kiara.android.services;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.events.AuthCodeGrantRequestEvent;
import uk.co.yojan.kiara.android.events.AuthCodeGrantResponse;
import uk.co.yojan.kiara.android.events.RefreshAccessTokenRequest;
import uk.co.yojan.kiara.android.events.RefreshAccessTokenResponse;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.data.AuthorizationCodeGrant;
import uk.co.yojan.kiara.client.data.RefreshAccessToken;

/*
 * EventBus service.
 *
 * Methods that are subscribed to listen to events on the bus relating to
 * Spotify Authentication.
 * -- Getting an Access Token and Refresh Token.
 * -- Refreshing the Access Token given a Refresh Token.
 */
public class SpotifyAuthService {

  private KiaraApiInterface api;
  private Bus bus;

  public SpotifyAuthService(KiaraApiInterface api, Bus bus) {
    this.api = api;
    this.bus = bus;
  }

  @Subscribe
  public void onAuthCodeGrantRequest(AuthCodeGrantRequestEvent event) {
    AuthorizationCodeGrant credentials = api.authorizeCode(event.getCode());

    bus.post(new AuthCodeGrantResponse(
        credentials.getAccessToken(),
        credentials.getRefreshToken(),
        credentials.getExpiresIn()));
  }

  @Subscribe
  public void onRefreshAccessTokenRequest(RefreshAccessTokenRequest event) {
    RefreshAccessToken credentials = api.refreshAccessToken(event.getRefreshToken());

    bus.post(new RefreshAccessTokenResponse(
        credentials.getAccessToken(),
        credentials.getExpiresIn()));
  }
}
