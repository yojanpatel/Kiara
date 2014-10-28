package uk.co.yojan.kiara.android.services;

import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.events.AuthCodeGrantRequest;
import uk.co.yojan.kiara.android.events.AuthCodeGrantResponse;
import uk.co.yojan.kiara.android.events.RefreshAccessTokenRequest;
import uk.co.yojan.kiara.android.events.RefreshAccessTokenResponse;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.SpotifyAuthInterface;
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

  private static final String log = SpotifyAuthService.class.getName();

  private SpotifyAuthInterface api;
  private Bus bus;

  public SpotifyAuthService(SpotifyAuthInterface api, Bus bus) {
    this.api = api;
    this.bus = bus;
  }

  @Subscribe
  public void onAuthCodeGrantRequest(AuthCodeGrantRequest event) {
    api.authorizeCode(event.getCode(), new Callback<AuthorizationCodeGrant>() {
      @Override
      public void success(AuthorizationCodeGrant credentials, Response response) {
        Log.i(log, credentials.getAccessToken() + ", " + credentials.getRefreshToken());

        bus.post(new AuthCodeGrantResponse(
            credentials.getAccessToken(),
            credentials.getRefreshToken(),
            credentials.getExpiresIn()));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.d(getClass().getName(), error.getMessage());
      }
    });


  }

  @Subscribe
  public void onRefreshAccessTokenRequest(RefreshAccessTokenRequest event) {
    api.refreshAccessToken(event.getRefreshToken(), new Callback<RefreshAccessToken>() {
      @Override
      public void success(RefreshAccessToken credentials, Response response) {
        bus.post(new RefreshAccessTokenResponse(
            credentials.getAccessToken(),
            credentials.getExpiresIn()));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.d(getClass().getName(), error.getMessage());
      }
    });
  }
}
