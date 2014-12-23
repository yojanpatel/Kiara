package uk.co.yojan.kiara.android.client;

import android.content.Context;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.example.android.GsonRequest;
import uk.co.yojan.kiara.client.AccessTokenCallback;
import uk.co.yojan.kiara.client.data.Playlist;
import uk.co.yojan.kiara.client.data.PlaylistWithSongs;
import uk.co.yojan.kiara.client.data.Song;
import uk.co.yojan.kiara.client.data.User;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KiaraClient {

  private static final String base = "https://kiara-yojan.appspot.com/";
  private VolleySingleton volley;
  private AccessTokenCallback callback;

  public KiaraClient(Context context, AccessTokenCallback callback) {
    volley = VolleySingleton.getInstance(context);
    this.callback = callback;
  }



  /*
   * Get the user resource from Kiara.
   */
  public void getUser(String userId, Response.Listener<User> success, Response.ErrorListener error) {
    GsonRequest<User> userReq = new GsonRequest<User>(
        Request.Method.GET,
        base + "users/" + userId,
        User.class,
        success,
        error
    ) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return defaultHeaders();
      }
    };

    volley.addToRequestQueue(userReq);
  }


  /*
   * Get all playlists for the user as an array.
   */
  public void allPlaylists(String userId, Response.Listener<Playlist[]> success, Response.ErrorListener error) {
    GsonRequest<Playlist[]> playlistReq = new GsonRequest<Playlist[]>(
        Request.Method.GET,
        base + "users/" + userId + "/playlists",
        Playlist[].class,
        success,
        error
    ) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return defaultHeaders();
      }
    };
    volley.addToRequestQueue(playlistReq);
  }


  /*
   * Get all playlists with the containing songs as well.
   */
  public void allPlaylistsWithSongs(String userId, Response.Listener<PlaylistWithSongs[]> success, Response.ErrorListener error) {
    Log.d("AllPlaylistsWithSongs", "All playlists with songs requested!");
    GsonRequest<PlaylistWithSongs[]> playlistReq = new GsonRequest<PlaylistWithSongs[]>(
        Request.Method.GET,
        base + "users/" + userId + "/playlists?detail=true",
        PlaylistWithSongs[].class,
        success,
        error
    ) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return defaultHeaders();
      }
    };
    volley.addToRequestQueue(playlistReq);
  }


  /*
   * Get all songs for a given playlist as an array.
   */
  public void getAllSongs(String userId, long playlistId, Response.Listener<Song[]> success, Response.ErrorListener error) {
    GsonRequest<Song[]> songReq = new GsonRequest<Song[]>(
        Request.Method.GET,
        base + "users/" + userId + "/playlists/" + playlistId + "/songs",
        Song[].class,
        success,
        error
    ) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        return defaultHeaders();
      }
    };
    volley.addToRequestQueue(songReq);
  }


  /*
   * Add a song to a playlist using its spotify id.
   *
  public void addSong(String userId, long playlistId, final String spotifyId,
                      Response.Listener<Song> success, Response.ErrorListener error) {

    GsonRequest<Song> songReq = new GsonRequest<Song>(
        Request.Method.POST,
        base + "users/" + userId + "/playlists/" + playlistId + "/songs",
        Song.class,
        success,
        error
    ) {
      @Override
      public byte[] getBody() throws AuthFailureError {
        Log.d("KiaraClient", spotifyId + ", " + Arrays.toString(spotifyId.getBytes()));
        return spotifyId.getBytes();
      }

      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("content-type", "text-plain");
        return headers;
      }
    };
    volley.addToRequestQueue(songReq);
  }
*/

  public void addSong(String userId, long playlistId, final String spotifyId,
                      Response.Listener<String> success, Response.ErrorListener error) {
    StringRequest sr = new StringRequest(
        Request.Method.POST,
        base + "users/" + userId + "/playlists/" + playlistId + "/songs",
        success,
        error
        ) {
      @Override
      public byte[] getBody() throws AuthFailureError {
        Log.d("KiaraClient", spotifyId + ", " + Arrays.toString(spotifyId.getBytes()));
        return spotifyId.getBytes();
      }

      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = defaultHeaders();
        headers.put("content-type", "text-plain");
        return headers;
      }
    };
    volley.addToRequestQueue(sr);
  }

  /*
   * Add a new playlist given an object. Gson is used to convert it to bytes.
   */
  public void createPlaylist(String userId, Playlist playlist,
                             Response.Listener<Playlist> success, Response.ErrorListener error) {
    GsonRequest<Playlist> playlistReq = null;
    try {
      playlistReq = new GsonRequest<Playlist>(
          Request.Method.POST,
          base + "users/" + userId + "/playlists",
          Playlist.class,
          playlist,
          success,
          error
      ) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
          HashMap<String, String> headers = defaultHeaders();
          headers.put("Content-Type", "application/json");
          headers.put("Accept", "application/json");
          return headers;
        }
      };
      volley.addToRequestQueue(playlistReq);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  public ArrayList<Song> getCachedSongs(String userId, long playlistId) {
    Song[] cachedSongs = volley.getFromCache(
        base + "users/" + userId + "/playlists/" + playlistId + "/songs",
        Song[].class);
    return cachedSongs == null ? null : new ArrayList<Song>(Arrays.asList(cachedSongs));
  }

  private HashMap<String, String> defaultHeaders() {
    HashMap<String, String> headers = new HashMap<String, String>();
    headers.put("Authorization", callback.getAccessToken());
    return headers;
  }
}
