package uk.co.yojan.kiara.server;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.authentication.AuthorizationCodeGrantRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.methods.authentication.RefreshAccessTokenRequest;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.ClientCredentials;
import com.wrapper.spotify.models.RefreshAccessTokenCredentials;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;


public class SpotifyApi {
  private static Logger log = Logger.getLogger(SpotifyApi.class.getName());

  private static Api api;
  private static long tokenExpiry = Long.MAX_VALUE;

  public static Api getApi() {
    if(api == null) {
      api = Api.builder()
          .clientId(ApiConstants.CLIENT_ID)
          .clientSecret(ApiConstants.CLIENT_SECRET)
          .redirectURI(ApiConstants.REDIRECT_URI)
          .refreshToken("placeholder")
          .build();
    }
    return api;
  }

  public static Api clientCredentialsApi() {
    api = getApi();

    // Access Token has expired. Generate a new one.
    if(tokenExpiry < getCurrentTimestamp()) {
      final ClientCredentialsGrantRequest request =
          api.clientCredentialsGrant().build();
      final SettableFuture<ClientCredentials> responseFuture = request.getAsync();
      // Add callbacks to handle success and failure of the async call.
      Futures.addCallback(responseFuture, new FutureCallback<ClientCredentials>() {
        @Override
        public void onSuccess(ClientCredentials c) {
          log.info("Successfully retrieved access token. " + c.getAccessToken());
          log.info("Access token expires in " + c.getExpiresIn() + " seconds.");
          tokenExpiry = getCurrentTimestamp() + (c.getExpiresIn() * 1000);
          api.setAccessToken(c.getAccessToken());
        }

        @Override
        public void onFailure(Throwable throwable) {
          log.warning("Error getting access token. " + throwable.getMessage());
        }
      });
    }
    return api;
  }

  public static AuthorizationCodeCredentials authorizationCodeGrant(String code) throws IOException, WebApiException {
    Api api = getApi();
    final AuthorizationCodeGrantRequest req = api.authorizationCodeGrant(code).build();
    return req.get();
  }

  public static RefreshAccessTokenCredentials getAccessTokenFromRefreshToken(String refreshToken)
      throws IOException, WebApiException {
    Api api = getApi();
    // TODO: modify spotify library to get rid of this check.
    api.setRefreshToken(refreshToken);
    final RefreshAccessTokenRequest req = api.refreshAccessToken().build();
    return req.get();
  }

  private static long getCurrentTimestamp() {
    return new Date().getTime();
  }

}
