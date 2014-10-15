package uk.co.yojan.kiara.android;

import android.app.Application;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import uk.co.yojan.kiara.android.services.SpotifyAuthService;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.KiaraClient;
import uk.co.yojan.kiara.client.SpotifyAuthInterface;

public class KiaraApplication extends Application {

  private SpotifyAuthService spotifyAuthService;
  KiaraApiInterface kiaraApi;
  SpotifyAuthInterface authApi;
  private Bus bus = new Bus(ThreadEnforcer.ANY);

  @Override
  public void onCreate() {
    super.onCreate();

    kiaraApi = KiaraClient.getKiaraApiClient();
    authApi = KiaraClient.getSpotifyAuth();
    spotifyAuthService = new SpotifyAuthService(authApi, bus);

    bus.register(spotifyAuthService);
    bus.register(this); // Listen to global events.
  }

  public Bus getBus() {
    return bus;
  }

}
