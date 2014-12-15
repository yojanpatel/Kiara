package uk.co.yojan.kiara.client;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Kiara Learning Interface for events, reward emissions.
 */
public interface KiaraLearningInterface {

  @POST("/events/{userId}/{playlistId}/start/{songId}")
  public void trackStarted(@Path("userId") String userId,
                           @Path("playlistId") Long playlistId,
                           @Path("songId") String songId,
                           Callback<String> cb);

  @POST("/events/{userId}/{playlistId}/finish/{songId}")
  public void trackFinished(@Path("userId") String userId,
                            @Path("playlistId") Long playlistId,
                            @Path("songId") String songId,
                            Callback<String> cb);

  @POST("/events/{userId}/{playlistId}/skip/{songId}/{skipTime}")
  public void trackSkipped(@Path("userId") String userId,
                           @Path("playlistId") Long playlistId,
                           @Path("songId") String songId,
                           @Path("skipTime") int skipTime,
                           Callback<String> cb);

  @POST("/events/{userId}/{playlistId}/queue/{currentSongId}/{queuedSongId}")
  public void trackQueued(@Path("userId") String userId,
                          @Path("playlistId") Long playlistId,
                          @Path("currentSongId") String currentSongId,
                          @Path("queuedSongId") String queuedSongId,
                          Callback<String> cb);

  @POST("/events/{userId}/{playlistId}/favourite/{songId}")
  public void trackFavourited(@Path("userId") String userId,
                              @Path("playlistId") Long playlistId,
                              @Path("songId") String songId,
                              Callback<String> cb);
}
