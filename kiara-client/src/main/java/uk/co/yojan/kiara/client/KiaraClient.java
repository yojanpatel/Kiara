package uk.co.yojan.kiara.client;

import retrofit.RestAdapter;

public class KiaraClient {

  // Singletons
  private static KiaraApiInterface sKiaraApi;
  private static SpotifyAuthInterface spotifyAuthApi;

  public static KiaraApiInterface getKiaraApiClient() {
    if(sKiaraApi == null) {
      RestAdapter restAdapter = new RestAdapter.Builder()
          .setEndpoint("http://kiara-yojan.appspot.com")
          .setLogLevel(RestAdapter.LogLevel.FULL)
          .build();

      sKiaraApi = restAdapter.create(KiaraApiInterface.class);
    }
    return sKiaraApi;
  }

  public static SpotifyAuthInterface getSpotifyAuth() {
    if(spotifyAuthApi == null) {
      RestAdapter restAdapter = new RestAdapter.Builder()
          .setEndpoint("http://kiara-spotify-auth.appspot.com")
          .setLogLevel(RestAdapter.LogLevel.FULL)
          .build();
      spotifyAuthApi = restAdapter.create(SpotifyAuthInterface.class);
    }
    return spotifyAuthApi;
  }
}