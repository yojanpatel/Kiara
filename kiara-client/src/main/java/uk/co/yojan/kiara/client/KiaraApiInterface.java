package uk.co.yojan.kiara.client;

import com.squareup.okhttp.Response;
import retrofit.Callback;
import retrofit.http.*;
import uk.co.yojan.kiara.client.data.Playlist;
import uk.co.yojan.kiara.client.data.PlaylistWithSongs;
import uk.co.yojan.kiara.client.data.Song;
import uk.co.yojan.kiara.client.data.User;

import java.util.Collection;

public interface KiaraApiInterface {

  @GET("/users/{userId}")
  public User getUser(@Path("userId") String userId);

  @DELETE("/users/{userId}")
  public Response deleteUser(@Path("userId") String userId);

  @POST("/users")
  public Response createUser(@Body User user);

  @PUT("/users/{userId}")
  public User updateUser(@Path("userId") String userId,
                         @Body User updated);


  @GET("/users/{userId}/playlists")
  public Collection<Playlist> getAllPlaylistsForUser(@Path("userId") String userId);

  @GET("/users/{userId}/playlists/{playlistId}")
  public Playlist getPlaylist(@Path("userId") String userId,
                              @Path("playlistId") Long playlistId);
  @GET("/users/{userId}/playlists/{playlistId}?detail=true")

  public PlaylistWithSongs getPlaylistWithSongs(@Path("userId") String userId,
                                                @Path("playlistId") Long playlistId);

  @DELETE("/users/{userId}/playlists/{playlistId}")
  public Response deletePlaylist(@Path("userId") String userId,
                             @Path("playlistId") Long playlistId);

  @POST("/users/{userId}/playlists/")
  public Response createPlaylist(@Path("userId") String userId,
                             @Body Playlist playlist);

  @PUT("/users/{userId}/playlists/{playlistId}")
  public Playlist updatePlaylist(@Path("userId") String userId,
                                 @Body Playlist updated);


  @GET("/users/{userId}/songs")
  public Collection<Song> getAllSongs(@Path("userId") String userId);

  @GET("/users/{userId}/songs/{songId}")
  public Song getSong(@Path("userId") String userId,
                      @Path("songId") Long songId);


  @DELETE("/users/{userId}/songs/{songId}")
  public Response deleteSong(@Path("userId") String userId,
                         @Path("songId") Long songId);

  @POST("/users/{userId}/songs")
  @Headers("content-type: text/plain")
  public Song addSong(@Path("userId") String userId,
                      @Body String spotifyId);


  @GET("/users/{userId}/playlists/{playlistId}/songs")
  public Collection<Song> getAllSongs(@Path("userId") String userId,
                                      @Path("playlistId") Long playlistId);

  @POST("/users/{userId}/playlists/{playlistId}/songs")
  @Headers("content-type: text/plain")
  public Song addSong(@Path("userId") String userId,
                      @Path("playlistId") Long playlistId,
                      @Body String spotifyId);


  /*
   * Each method shown above has an equivalent with a callback argument to allow asynchronous calls.
   */

  @GET("/users/{userId}/")
  public void getUser(@Path("userId") String userId,
                      Callback<User> cb);

  @DELETE("/users/{userId}/")
  public void deleteUser(@Path("userId") String userId,
                         Callback<Response> cb);

  @POST("/users/")
  public void createUser(@Body User user,
                         Callback<Response> cb);

  @PUT("/users/{userId}")
  public void updateUser(@Path("userId") String userId,
                         @Body User updated,
                         Callback<User> cb);


  @GET("/users/{userId}/playlists")
  public void getAllPlaylistsForUser(@Path("userId") String userId,
                                     Callback<Collection<Playlist>> cb);

  @GET("/users/{userId}/playlists/{playlistId}")
  public void getPlaylist(@Path("userId") String userId,
                              @Path("playlistId") Long playlistId,
                              Callback<Playlist> cb);

  @GET("/users/{userId}/playlists/{playlistId}?detail=true")
  public void getPlaylistWithSongs(@Path("userId") String userId,
                                   @Path("playlistId") Long playlistId,
                                   Callback<PlaylistWithSongs> cb);

  @DELETE("/users/{userId}/playlists/{playlistId}")
  public void deletePlaylist(@Path("userId") String userId,
                             @Path("playlistId") Long playlistId,
                             Callback<Response> cb);

  @POST("/users/{userId}/playlists/")
  public void createPlaylist(@Path("userId") String userId,
                             @Body Playlist playlist,
                             Callback<Response> cb);

  @PUT("/users/{userId}/playlists/{playlistId}")
  public void updatePlaylist(@Path("userId") String userId,
                             @Body Playlist updated,
                             Callback<Playlist> cb);


  @GET("/users/{userId}/songs")
  public void getAllSongs(@Path("userId") String userId,
                          Callback<Collection<Song>> cb);

  @GET("/users/{userId}/songs/{songId}")
  public void getSong(@Path("userId") String userId,
                      @Path("songId") Long songId,
                      Callback<Song> cb);


  @DELETE("/users/{userId}/songs/{songId}")
  public void deleteSong(@Path("userId") String userId,
                         @Path("songId") Long songId,
                         Callback<Response> cb);

  @POST("/users/{userId}/songs")
  @Headers("content-type: text/plain")
  public void addSong(@Path("userId") String userId,
                      @Body String spotifyId,
                      Callback<Song> cb);


  @GET("/users/{userId}/playlists/{playlistId}/songs")
  public void getAllSongs(@Path("userId") String userId,
                          @Path("playlistId") Long playlistId,
                          Callback<Collection<Song>> cb);

  @POST("/users/{userId}/playlists/{playlistId}/songs")
  @Headers("content-type: text/plain")
  public void addSong(@Path("userId") String userId,
                      @Path("playlistId") Long playlistId,
                      @Body String spotifyId,
                      Callback<Song> cb);
}
