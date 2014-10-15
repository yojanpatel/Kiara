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

  public static void main(String[] args) {
    String s = "AQDJ4nlhW_-nnM_FfmRuZbM7zGTt_stgtNo1B9nLef-lCFA1QVh2G6bulJ38a3VBQgjwetO64luvGketZY3d05lYCa3jWHij0zrXWF5Oird46PMQ_ck1GB1iom--LYth0TKGfpglAT9ky0e8c2mWmBK2BKRPLkBZSUUVJLn33AS-AeJFj3ZU3eb92h3Q3hyf0oJ1uYj9myhggSrtYXx6xrFbDaY";
    System.out.println(getSpotifyAuth().authorizeCode(s));
  }
}