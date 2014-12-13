package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.PlayerControlFragment;
import uk.co.yojan.kiara.android.fragments.PlaylistListFragment;

import java.net.MalformedURLException;
import java.net.URL;

public class PlaylistViewActivity extends KiaraActivity {
    private static final String log = PlaylistViewActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_playlist_view);

      Intent trigger = getIntent();
      String urlData = trigger.getStringExtra(Intent.EXTRA_TEXT);
      String subjData = trigger.getStringExtra(Intent.EXTRA_SUBJECT);
      if(urlData != null) {
        setTitle("Choose playlist to add into");
      }
      Bundle args = intialiseArgs(urlData, subjData);
      if (savedInstanceState == null) {
        getFragmentManager().beginTransaction()
            .add(R.id.container, PlaylistListFragment.newInstance(args)).commit();
      } else {
        Log.d(log, "Fragment PlaylistListFragment already attached.");
        if(args != null) {
          getFragmentManager().findFragmentById(R.id.container).setArguments(args);
        }
      }
    }

  @Override
  protected void onResume() {
    Log.d(log, "onResume");
    super.onResume();
    if(sharedPreferences().getBoolean(Constants.IN_SESSION, false)) {
      Log.d(log, "adding control fragment to activity.");
      getFragmentManager().beginTransaction()
          .add(R.id.controller_container, PlayerControlFragment.newInstance()).commit();
    }
  }

  /* Spotify sharing URL patterns
     * http://open.spotify.com/user/{userId}/playlist/{playlistId}
     * http://open.spotify.com/album/{albumId}
     * http://open.spotify.com/track/{trackId} */
  private Bundle intialiseArgs(String url, String title) {
    if (url != null) {
      Bundle args = new Bundle();
      Log.d(log, url);
      try {
        URL spotifyURL = new URL(url);
        String[] parts = spotifyURL.getFile().split("/");
        Log.d(log, spotifyURL.toString() + " " + parts.length);
        for (String part : parts) {
          Log.d(log, part);
        }
        // CASE playlist
        if (parts.length == 5) {
          String user = parts[2];
          String playlistId = parts[4];
          Log.d(log, "Playlist shared: " + user + " " + playlistId);
          args.putString(PlaylistListFragment.USER_PARAM, user);
          args.putString(PlaylistListFragment.PLAYLIST_PARAM, playlistId);
          args.putString(PlaylistListFragment.PLAYLIST_NAME_PARAM, title);
          args.putSerializable(PlaylistListFragment.CASE_PARAM, Constants.Case.Playlist);
        }
        // CASE album
        else if (parts[1].equals("album")) {
          String albumId = parts[2];
          Log.d(log, "Album shared: " + albumId);
          args.putString(PlaylistListFragment.ALBUM_PARAM, albumId);
          args.putSerializable(PlaylistListFragment.CASE_PARAM, Constants.Case.Album);
        }
        // CASE track
        else if (parts[1].equals("track")) {
          String trackId = parts[2];
          Log.d(log, "Track shared: " + trackId);
          args.putString(PlaylistListFragment.TRACK_PARAM, trackId);
          args.putSerializable(PlaylistListFragment.CASE_PARAM, Constants.Case.Track);
        }
      } catch (MalformedURLException e) {
        Log.e(log, e.toString());
      }
      return args;
    }
    return null;
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.playlist_view, menu);
      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.action_settings) {
          return true;
      }
      return super.onOptionsItemSelected(item);
  }
}
