package uk.co.yojan.kiara.client;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class KiaraClient {

  // Singletons
  private static KiaraApiInterface sKiaraApi;
  private static KiaraLearningInterface sKiaraLearningApi;
  private static SpotifyAuthInterface spotifyAuthApi;
  private static SpotifyApiInterface spotifyApi;


  /*
   * Interface to interact with various Kiara related methods.
   */
  public static KiaraApiInterface getKiaraApiClient(final AccessTokenCallback callback) {
    if(sKiaraApi == null) {
      RestAdapter restAdapter = new RestAdapter.Builder()
          .setEndpoint("https://kiara-yojan.appspot.com")
//          .setEndpoint("http://localhost:8080")
          .setLogLevel(RestAdapter.LogLevel.FULL)
          .setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
              request.addHeader("Authorization", callback.getAccessToken());
            }
          }).build();

      sKiaraApi = restAdapter.create(KiaraApiInterface.class);
    }
    return sKiaraApi;
  }

  public static KiaraLearningInterface getKiaraLearningClient(final AccessTokenCallback callback) {
    if(sKiaraLearningApi == null) {
      RestAdapter restAdapter = new RestAdapter.Builder()
//                    .setEndpoint("http://localhost:8080")
          .setEndpoint("https://kiara-analysis-dot-kiara-yojan.appspot.com")
          .setLogLevel(RestAdapter.LogLevel.NONE)
          .setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
              request.addHeader("Authorization", callback.getAccessToken());
            }
          }).build();
      sKiaraLearningApi = restAdapter.create(KiaraLearningInterface.class);
    }
    return sKiaraLearningApi;
  }


  /*
   * This interface is to interact with the auth service hosted on app-engine
   * to get access tokens from refresh tokens.
   */
  public static SpotifyAuthInterface getSpotifyAuth() {
    if(spotifyAuthApi == null) {
      RestAdapter restAdapter = new RestAdapter.Builder()
          .setEndpoint("https://auth-dot-kiara-yojan.appspot.com")
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
  public static SpotifyApiInterface getSpotifyApi(final AccessTokenCallback callback) {
    if(spotifyApi == null) {
    RestAdapter restAdapter = new RestAdapter.Builder()
        .setEndpoint("https://api.spotify.com")
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setRequestInterceptor(new RequestInterceptor() {
          @Override
          public void intercept(RequestFacade request) {
            request.addHeader("Authorization", "Bearer " + callback.getAccessToken());
          }
        }).build();
      spotifyApi = restAdapter.create(SpotifyApiInterface.class);
    }

    return spotifyApi;
  }
}