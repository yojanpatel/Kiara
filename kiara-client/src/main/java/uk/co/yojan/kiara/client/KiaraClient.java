package uk.co.yojan.kiara.client;

import retrofit.RestAdapter;

public class KiaraClient {

  // Singleton
  private static KiaraApiInterface sKiaraApi;

  public static KiaraApiInterface getKiaraApiClient() {
    if(sKiaraApi == null) {
      RestAdapter restAdapter = new RestAdapter.Builder()
          .setEndpoint("http://localhost:8080")
          .setLogLevel(RestAdapter.LogLevel.FULL)
          .build();

      sKiaraApi = restAdapter.create(KiaraApiInterface.class);
    }

    return sKiaraApi;
  }
}