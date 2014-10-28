package uk.co.yojan.kiara.client;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class KiaraClient {

  // Singletons
  private static KiaraApiInterface sKiaraApi;
  private static SpotifyAuthInterface spotifyAuthApi;


  /*
   * Interface to interact with various Kiara related methods.
   */
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


  /*
   * This interface is to interact with the auth service hosted on app-engine
   * to get access tokens from refresh tokens.
   */
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


  /*
   * This interface provides requests to interact with the Spotify Web API
   * to query various meta-data related to songs, albums, artists etc.
   */
  public static SpotifyApiInterface getSpotifyApi(final String accessToken) {
    RestAdapter.Builder builder = new RestAdapter.Builder()
        .setEndpoint("https://api.spotify.com")
        .setLogLevel(RestAdapter.LogLevel.FULL);
    if(accessToken != null) {
      builder.setRequestInterceptor(new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
          request.addHeader("Authorization", "Bearer " + accessToken);
        }
      });
    }

    return builder.build().create(SpotifyApiInterface.class);
  }
}