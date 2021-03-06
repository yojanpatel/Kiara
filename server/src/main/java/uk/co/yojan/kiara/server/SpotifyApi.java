package uk.co.yojan.kiara.server;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.ClientCredentials;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;


public class SpotifyApi {
  private static Logger log = Logger.getLogger(SpotifyApi.class.getName());

  private static Api clientCredentialsApi;
  private static long tokenExpiry = Long.MAX_VALUE;

  public static Api clientCredentialsApi() {
    if(clientCredentialsApi == null) {
      clientCredentialsApi = Api.builder()
          .clientId(Constants.CLIENT_ID)
          .clientSecret(Constants.CLIENT_SECRET)
          .build();
    }

    // Access Token has expired. Generate a new one.
    if(tokenExpiry < getCurrentTimestamp()) {
      final ClientCredentialsGrantRequest request =
          clientCredentialsApi.clientCredentialsGrant().build();
      final SettableFuture<ClientCredentials> responseFuture = request.getAsync();
      // Add callbacks to handle success and failure of the async call.
      Futures.addCallback(responseFuture, new FutureCallback<ClientCredentials>() {
        @Override
        public void onSuccess(ClientCredentials c) {
          log.info("Successfully retrieved access token. " + c.getAccessToken());
          log.info("Access token expires in " + c.getExpiresIn() + " seconds.");
          tokenExpiry = getCurrentTimestamp() + (c.getExpiresIn() * 1000);
          clientCredentialsApi.setAccessToken(c.getAccessToken());
        }

        @Override
        public void onFailure(Throwable throwable) {
          log.warning("Error getting access token. " + throwable.getMessage());
        }
      });
    }
    return clientCredentialsApi;
  }


  public static Api spotifyApi() throws IOException, WebApiException {
    clientCredentialsApi().setAccessToken("BQCxnvsO7-V3_sCEAM9VbWMqazT4rBIUGOcCUbgyIrJCexOel58W8sAclqjFsRV2pA-B9AqmlXGkt0xbSi9KUqRoHkHVirAHRCeFxXB52KLzJI2LqfcetwGphjBHRqKV1NbV11OTutdA_9HBvfueRG7irjcWHco4FHkxLuCMlmNETc7BIYo");
    return clientCredentialsApi();
  }



  private static long getCurrentTimestamp() {
    return new Date().getTime();
  }
}
