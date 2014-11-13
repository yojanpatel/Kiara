package uk.co.yojan.kiara.android;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;
import retrofit.client.OkClient;
import uk.co.yojan.kiara.android.events.AuthCodeGrantResponse;
import uk.co.yojan.kiara.android.events.RefreshAccessTokenResponse;
import uk.co.yojan.kiara.android.services.KiaraService;
import uk.co.yojan.kiara.android.services.SpotifyAuthService;
import uk.co.yojan.kiara.android.services.SpotifyWebService;
import uk.co.yojan.kiara.android.utils.OttoEventBuffer;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.KiaraClient;
import uk.co.yojan.kiara.client.SpotifyApiInterface;
import uk.co.yojan.kiara.client.SpotifyAuthInterface;

import java.io.File;
import java.io.IOException;

public class KiaraApplication extends Application {

  private static final String log = KiaraApplication.class.getName();

  private EncryptedSharedPreferences sharedPreferences;

  private SpotifyAuthService spotifyAuthService;
  private SpotifyWebService spotifyWebService;
  private KiaraService kiaraService;

  private KiaraApiInterface kiaraApi;
  private SpotifyAuthInterface authApi;
  private SpotifyApiInterface spotifyWebApi;
  private uk.co.yojan.kiara.android.client.KiaraClient client;

  private Bus bus = new Bus(ThreadEnforcer.ANY);
  private OttoEventBuffer eventBuffer;

  @Override
  public void onCreate() {
    super.onCreate();
    sharedPreferences = EncryptedSharedPreferences.getPrefs(this, Constants.PREFERENCE_STRING, Context.MODE_PRIVATE);

    // Construct the Api for various interactions.
    client = new uk.co.yojan.kiara.android.client.KiaraClient(getApplicationContext());
    kiaraApi = KiaraClient.getKiaraApiClient();
    authApi = KiaraClient.getSpotifyAuth();

    // Create the services that subscribe to the event bus.
    spotifyAuthService = new SpotifyAuthService(authApi, bus);

    // Instantiate event buffer wrt to the global event bus.
    eventBuffer = new OttoEventBuffer(bus);
    eventBuffer.startSaving();

    bus.register(spotifyAuthService);
    bus.register(this); // Listen to global events.
  }

  public Bus getBus() {
    return bus;
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
  public SpotifyApiInterface spotifyApi() {
    return spotifyWebApi;
  }

  public SpotifyApiInterface updateSpotifyService(final String accessToken) {
    Log.d(log, "Updating the access token. Unregistering the Spotify web service and re-registering with the updated one.");
    if(spotifyWebService != null) {
      bus.unregister(spotifyWebService);
    }
    spotifyWebApi =  KiaraClient.getSpotifyApi(accessToken);
    spotifyWebService = new SpotifyWebService(spotifyWebApi, bus);
    bus.register(spotifyWebService);
    return spotifyWebApi;
  }

  // Instantiate the KiaraService to handle the REST calls with Kiara server.
  public void initKiaraService(String userId) {
    Log.d(log, "Initializing the Kiara Service and registering to the event bus.");
    kiaraService = new KiaraService(kiaraApi, bus, userId, client);
    bus.register(kiaraService);
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

}
