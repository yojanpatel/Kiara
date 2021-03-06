package uk.co.yojan.kiara.client;

import retrofit.Callback;
import retrofit.http.*;
import uk.co.yojan.kiara.client.data.ActionEvent;
import uk.co.yojan.kiara.client.data.Song;

/**
 * Kiara Learning Interface for events, reward emissions.
 */
public interface KiaraLearningInterface {

  @POST("/events/{userId}/{playlistId}/start/{songId}")
  public void trackStarted(@Path("userId") String userId,
                           @Path("playlistId") Long playlistId,
                           @Path("songId") String songId,
                           Callback<Song> cb);

  @POST("/events/{userId}/{playlistId}/finish/{songId}")
  public void trackFinished(@Path("userId") String userId,
                            @Path("playlistId") Long playlistId,
                            @Path("songId") String songId,
                            Callback<Song> cb);

  @POST("/events/{userId}/{playlistId}/skip/{songId}/{skipTime}")
  public void trackSkipped(@Path("userId") String userId,
                           @Path("playlistId") Long playlistId,
                           @Path("songId") String songId,
                           @Path("skipTime") int skipTime,
                           Callback<Song> cb);

  @POST("/events/{userId}/{playlistId}/queue/{currentSongId}/{queuedSongId}")
  public void trackQueued(@Path("userId") String userId,
                          @Path("playlistId") Long playlistId,
                          @Path("currentSongId") String currentSongId,
                          @Path("queuedSongId") String queuedSongId,
                          Callback<Song> cb);

  @POST("/events/{userId}/{playlistId}/favourite/{songId}")
  public void trackFavourited(@Path("userId") String userId,
                              @Path("playlistId") Long playlistId,
                              @Path("songId") String songId,
                              Callback<Song> cb);

  @POST("/events/{userId}/{playlistId}")
  public void transition(@Path("userId") String userId,
                         @Path("playlistId") Long playlistId,
                         @Body ActionEvent event,
                         Callback<Song> cb);

  @GET("/events/{userId}/{playlistId}/recommend")
  public void recommend(@Path("userId") String userId,
                        @Path("playlistId") Long playlistId,
                        @Query("s") String songId,
                        Callback<Song> cb);

  @GET("/features/history/{playlistId}")
  public String getHistory(@Path("playlistId") long playlistId);
}
