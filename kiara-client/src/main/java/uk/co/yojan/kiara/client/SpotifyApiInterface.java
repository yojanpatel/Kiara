package uk.co.yojan.kiara.client;


import retrofit.Callback;
import retrofit.http.*;
import uk.co.yojan.kiara.client.data.spotify.*;

import java.util.Map;

public interface SpotifyApiInterface {

  @GET("/v1/search?type=album,artist,track")
  @Headers("Accept: application/json")
  public SearchResult search(@Query("q") String query,
                             @Query("limit") int limit,
                             @Query("offset") int offset);

  @GET("/v1/search?type=album,artist,track")
  @Headers("Accept: application/json")
  public void search(@Query("q") String query,
                             @Query("limit") int limit,
                             @Query("offset") int offset,
                             Callback<SearchResult> cb);

  @GET("/v1/artists/{id}/albums")
  public void getArtistAlbums(@Path("id") String artistId, @QueryMap Map<String, String> options, Callback<Pager<Album>> cb);


  // Requires oAuth (interceptor adds additional header).
  @GET("/v1/me")
  public SpotifyUser getCurrentUser();

  @GET("/v1/me")
  public void getCurrentUser(Callback<SpotifyUser> cb);

  @GET("/v1/users/{userId}/playlists/{playlistId}?fields=(images.url,tracks.items(track(album(name),artists(name),id,name)))")
  public void getTrackIdsForPlaylist(@Path("userId") String userId,
                                     @Path("playlistId") String playlistId,
                                     Callback<Playlist> cb);


  @GET("/v1/users/{userId}/playlists/")
  public void getPlaylistsForUser(@Path("userId") String userId,
                                     Callback<Pager<Playlist>> cb);

}
