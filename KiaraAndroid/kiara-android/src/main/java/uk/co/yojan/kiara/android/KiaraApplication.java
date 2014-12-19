package uk.co.yojan.kiara.android;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import uk.co.yojan.kiara.android.events.AuthCodeGrantResponse;
import uk.co.yojan.kiara.android.events.RefreshAccessTokenResponse;
import uk.co.yojan.kiara.android.services.KiaraService;
import uk.co.yojan.kiara.android.services.SpotifyAuthService;
import uk.co.yojan.kiara.android.services.SpotifyWebService;
import uk.co.yojan.kiara.android.utils.BusProvider;
import uk.co.yojan.kiara.android.utils.OttoEventBuffer;
import uk.co.yojan.kiara.client.*;

public class KiaraApplication extends Application {

  private static final String log = KiaraApplication.class.getName();

  private EncryptedSharedPreferences sharedPreferences;

  private SpotifyAuthService spotifyAuthService;
  private SpotifyWebService spotifyWebService;
  private KiaraService kiaraService;

  private KiaraApiInterface kiaraApi;
  private KiaraLearningInterface kiaraLearningApi;
  private SpotifyAuthInterface authApi;
  private SpotifyApiInterface spotifyWebApi;
  private uk.co.yojan.kiara.android.client.KiaraClient client;

  private OttoEventBuffer eventBuffer;

  @Override
  public void onCreate() {
    super.onCreate();
    sharedPreferences = EncryptedSharedPreferences.getPrefs(this, Constants.PREFERENCE_STRING, Context.MODE_PRIVATE);

    // Construct the Api for various interactions.
    client = new uk.co.yojan.kiara.android.client.KiaraClient(getApplicationContext());
    kiaraApi = KiaraClient.getKiaraApiClient();
    kiaraLearningApi = KiaraClient.getKiaraLearningClient();
    authApi = KiaraClient.getSpotifyAuth();

    // Create the services that subscribe to the event bus.
    spotifyAuthService = new SpotifyAuthService(authApi, getBus());

    // Instantiate event buffer wrt to the global event bus.
    eventBuffer = new OttoEventBuffer(getBus());
    eventBuffer.startSaving();

    getBus().register(spotifyAuthService);
    getBus().register(this); // Listen to global events.
  }

  public Bus getBus() {
    return BusProvider.getInstance();
  }

  public OttoEventBuffer eventBuffer() {
    return eventBuffer;
  }

  public SpotifyApiInterface spotifyWebApi() {
    return spotifyWebApi;
  }

  public KiaraApiInterface kiaraApi() {
    return kiaraApi;
  }

  public uk.co.yojan.kiara.android.client.KiaraClient kiaraClient() {
    return client;
  }

  public KiaraLearningInterface learningApi() {
    return kiaraLearningApi;
  }

  public SpotifyApiInterface spotifyApi() {
    return spotifyWebApi;
  }

  public SpotifyApiInterface updateSpotifyService(final String accessToken) {
    Log.d(log, "Updating the access token. Unregistering the Spotify web service and re-registering with the updated one.");
    if(spotifyWebService != null) {
      getBus().unregister(spotifyWebService);
    }
    spotifyWebApi =  KiaraClient.getSpotifyApi(accessToken);
    spotifyWebService = new SpotifyWebService(spotifyWebApi, getBus());
    getBus().register(spotifyWebService);
    return spotifyWebApi;
  }

  // Instantiate the KiaraService to handle the REST calls with Kiara server.
  public void initKiaraService(String userId) {
    if(kiaraService == null) {
      Log.d(log, "Initializing the Kiara Service and registering to the event bus.");
      kiaraService = new KiaraService(kiaraApi, getBus(), userId, client);
      getBus().register(kiaraService);
    }
  }

  public EncryptedSharedPreferences sharedPreferences() {
    return sharedPreferences;
  }


  @Subscribe
  public void onAuthCodeGrantComplete(AuthCodeGrantResponse credentials) {
    updateSpotifyService(credentials.getAccessToken());
  }

  @Subscribe
  public void onRefreshTokenComplete(RefreshAccessTokenResponse credentials) {
    updateSpotifyService(credentials.getAccessToken());
  }

  public String userId() {
    return sharedPreferences().getString(Constants.USER_ID, null);
  }

}
