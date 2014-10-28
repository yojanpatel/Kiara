package uk.co.yojan.kiara.android.services;

import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.yojan.kiara.android.events.GetPlaylistsRequest;
import uk.co.yojan.kiara.client.KiaraApiInterface;
import uk.co.yojan.kiara.client.SpotifyApiInterface;
import uk.co.yojan.kiara.client.data.Playlist;

import java.util.ArrayList;
import java.util.Collection;

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
    kiaraApi.getAllPlaylistsForUser(userId, new Callback<Collection<Playlist>>() {
      @Override
      public void success(Collection<Playlist> playlists, Response response) {
        bus.post(convertToList(playlists));
      }

      @Override
      public void failure(RetrofitError error) {
        Log.e(log, error.getMessage());

      }
    });
  }

  private ArrayList<Playlist> convertToList(Collection<Playlist> coll) {
    return new ArrayList<Playlist>(coll);
  }
}
