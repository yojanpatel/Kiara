package uk.co.yojan.kiara.android.services;

import android.util.Log;
import android.widget.Toast;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.events.*;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.SpotifyApiInterface;
import uk.co.yojan.kiara.client.data.Playlist;
import uk.co.yojan.kiara.client.data.PlaylistWithSongs;
import uk.co.yojan.kiara.client.data.Song;
import uk.co.yojan.kiara.client.data.spotify.SearchResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KiaraService {

  private static final String log = KiaraApiInterface.class.getName();

  private KiaraApiInterface kiaraApi;
  private Bus bus;
  private String userId;

  public KiaraService(KiaraApiInterface api, Bus bus, String userId) {
    this.kiaraApi = api;
    this.bus = bus;
    this.userId = userId;
  }

  @Subscribe
  public void onGetPlaylists(GetPlaylistsRequest request) {
    Log.d(log, "Requesting playlists for user.");
    kiaraApi.getAllPlaylistsWithSongs(userId, new Callback<List<PlaylistWithSongs>>() {
      @Override
      public void success(List<PlaylistWithSongs> playlists, Response response) {
        Log.d(log, "successfully got playlists. posting onto the bus.");
        bus.post(new ArrayList<PlaylistWithSongs>(playlists));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.getMessage());

      }
    });
  }

  @Subscribe
  public void onCreatePlaylist(CreatePlaylistRequest request) {
    Log.d(log, "Creating playlist " + request.getName() + " for user.");
    Playlist newPlaylist = new Playlist();
    newPlaylist.setPlaylistName(request.getName());
    kiaraApi.createPlaylist(userId, newPlaylist, new Callback<Playlist>() {
      @Override
      public void success(Playlist playlist, Response response) {
        bus.post(new CreatedPlaylist(playlist));
        Log.d(log, "Successfully created new playlist " + playlist.getPlaylistName());
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.getMessage());
      }
    });

  }

  @Subscribe
  public void onAddSong(AddSong as) {
    kiaraApi.addSong(userId, as.getPlaylistId(), as.getSongId(), new Callback<Song>() {
      @Override
      public void success(Song song, Response response) {
        Log.d(log, "Successfully added new song.");
        bus.post(new SongAdded(song));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.getMessage());
      }
    });
  }

  @Subscribe
  public void onSearchResult(SearchResultCreateSongEvent result) {
    Log.d(log, "Search Result received");

    int tracksReturned = result.getTracks().getTracks().size();
    if(tracksReturned == 0) return;

    String spotifyId = result.getTracks().getTracks().get(0).getId();
    kiaraApi.addSong(userId, result.getPlaylistId(), spotifyId, new Callback<Song>() {
      @Override
      public void success(Song song, Response response) {
        Log.d(log, "Successfully added new song.");
        bus.post(new SongAdded(song));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.getMessage());
      }
    } );
  }
}
