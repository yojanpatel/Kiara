package uk.co.yojan.kiara.android.services;

import android.util.Log;
import com.android.volley.VolleyError;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.client.KiaraClient;
import uk.co.yojan.kiara.android.events.*;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.data.Playlist;
import uk.co.yojan.kiara.client.data.PlaylistWithSongs;
import uk.co.yojan.kiara.client.data.Song;
import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class KiaraService {

  private static final String log = KiaraService.class.getName();

  private KiaraApiInterface kiaraApi;
  KiaraClient client;
  private Bus bus;
  private String userId;


  public KiaraService(KiaraApiInterface api, Bus bus, String userId, KiaraClient client) {
    this.kiaraApi = api;
    this.bus = bus;
    this.userId = userId;
    this.client = client;
  }


  @Subscribe
  public void onGetAllPlaylists(GetAllPlaylists req) {
    Log.d(log, "Requesting all playlists for user.");
    client.allPlaylistsWithSongs(userId,
        new com.android.volley.Response.Listener<PlaylistWithSongs[]>() {
          @Override
          public void onResponse(PlaylistWithSongs[] response) {

            Log.d(log, "Successfully got playlists. Posting onto the bus." + response[0].getPlaylist().getPlaylistName());
            bus.post(new ArrayList<PlaylistWithSongs>(Arrays.asList(response)));
          }
        },

        new com.android.volley.Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.e(log, error.toString());
          }
        }
    );
  }


  @Subscribe
  public void getSongsForPlaylist(GetSongsForPlaylist request) {
    Log.d(log, "Requesting songs for playlist " + request.getId());
    client.getAllSongs(userId, request.getId(),
        new com.android.volley.Response.Listener<Song[]>() {
          @Override
          public void onResponse(Song[] response) {
            if(response == null) return;
            Log.d(log, "Successfully got " + response.length + " songs for the playlist. Posting onto the bus.");
            bus.post(new ArrayList<Song>(Arrays.asList(response)));
          }
        },
        new com.android.volley.Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.e(log, error.toString());
          }
        }
    );
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
        Log.e(log, error.toString());
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
        Log.e(log, error.toString());
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
        Log.e(log, error.toString());
      }
    } );
  }

  @Subscribe
  public void onBatchAddSongs(BatchAddSongs request) {
    ArrayList<String> ids = new ArrayList<String>();
    for(Track t : request.getTracks()) ids.add(t.getId());
    kiaraApi.addSongs(userId, request.getPlaylistId(), ids, new Callback<List<Song>>() {
      @Override
      public void success(List<Song> songs, Response response) {
        Log.d(log, "Added " + songs.size() + " songs.");
        bus.post(new ArrayList<Song>(songs));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.toString());
      }
    });

  }


}
