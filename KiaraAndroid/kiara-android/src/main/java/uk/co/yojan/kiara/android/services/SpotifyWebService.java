package uk.co.yojan.kiara.android.services;

import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.events.CurrentUserRequest;
import uk.co.yojan.kiara.android.events.FetchPlaylistTracks;
import uk.co.yojan.kiara.android.events.SearchRequest;
import uk.co.yojan.kiara.client.SpotifyApiInterface;
import uk.co.yojan.kiara.client.data.spotify.SearchResult;
import uk.co.yojan.kiara.client.data.spotify.SpotifyUser;

/**
 * Service to handle requests to the Spotify Web Api.
 * e.g. get current user, search for tracks, etc.
 */
public class SpotifyWebService {

  private static final String log = SpotifyWebService.class.getName();

  private SpotifyApiInterface spotifyApi;
  private Bus bus;

  public SpotifyWebService(SpotifyApiInterface api, Bus bus) {
    this.spotifyApi = api;
    this.bus = bus;
  }

  public void setSpotifyApi(SpotifyApiInterface api) {
    this.spotifyApi = api;
  }

  @Subscribe
  public void onGetCurrentUser(CurrentUserRequest event) {
    spotifyApi.getCurrentUser(new Callback<SpotifyUser>() {
      @Override
      public void success(SpotifyUser user, Response r) {
        bus.post(user);
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.getMessage());
      }
    });
  }

  @Subscribe
  public void search(final SearchRequest request) {
    Log.d("SEARCH", request.getQuery());
    spotifyApi.search(request.getQuery(), request.getLimit(), request.getOffset(),
        new Callback<SearchResult>() {
          @Override
          public void success(SearchResult searchResult, Response response) {
            bus.post(searchResult);
          }

          @Override
          public void failure(RetrofitError error) {
            Log.e(log, error.getMessage());
          }
        });
  }

  @Subscribe
  public void getTracksForPlaylist(final FetchPlaylistTracks request) {
    String userId = request.getUserId();
    String playlistId = request.getPlaylistId();
    spotifyApi.getTrackIdsForPlaylist(userId, playlistId, new Callback<uk.co.yojan.kiara.client.data.spotify.Playlist>() {
      @Override
      public void success(uk.co.yojan.kiara.client.data.spotify.Playlist playlist, Response response) {
        bus.post(playlist);
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.getMessage());
      }
    });
  }
}
