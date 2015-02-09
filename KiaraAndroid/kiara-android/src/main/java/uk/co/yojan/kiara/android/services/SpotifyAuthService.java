package uk.co.yojan.kiara.android.services;

import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.events.*;
import uk.co.yojan.kiara.client.SpotifyAuthInterface;
import uk.co.yojan.kiara.client.data.AuthorizationCodeGrant;
import uk.co.yojan.kiara.client.data.RefreshAccessToken;

import java.util.HashMap;

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

  HashMap<Object, Integer> retries = new HashMap<Object, Integer>();

  @Subscribe
  public void onAuthCodeGrantRequest(final AuthCodeGrantRequest event) {
    if(!retries.containsKey(event)) {
      retries.put(event, 5);
    } else {
      retries.put(event, retries.get(event) - 1);
    }

    Log.d(log, event.getCode());
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
        if(retries.get(event) > 0)
          bus.post(event);
      }
    });


  }

  @Subscribe
  public void onRefreshAccessTokenRequest(RefreshAccessTokenRequest event) {
    api.refreshAccessToken(event.getRefreshToken(), event.getUserId(), new Callback<RefreshAccessToken>() {
      @Override
      public void success(RefreshAccessToken credentials, Response response) {
        Log.d(log, "SUCCESS");
        bus.post(new RefreshAccessTokenResponse(
            credentials.getAccessToken(),
            credentials.getExpiresIn()));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.d(getClass().getName(), error.getMessage());
        if(error.getResponse().getStatus() == 403) {
          Log.d("SpotifyAuthService", "Refresh Token did not match that on the server, must relogin.");
          bus.post(new ForceRelogin());
        }
      }
    });
  }
}
