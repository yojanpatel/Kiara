package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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

      Toolbar toolbar = getToolbar();
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      if(toolbar != null) {
        Log.d(log, "toolbar is not null.");
        ((TextView) toolbar.findViewById(R.id.toolbarTitle)).setText("Playlists");

      }

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
          String spotifyPlaylistId = parts[4];
          Log.d(log, "Playlist shared: " + user + " " + spotifyPlaylistId);
          args.putString(Constants.ARG_USER_ID, user);
          args.putString(Constants.ARG_PLAYLIST_SPOTIFY_ID, spotifyPlaylistId);
          args.putString(Constants.ARG_PLAYLIST_SPOTIFY_NAME, title);
          args.putSerializable(Constants.ARG_CASE, Constants.Case.Playlist);
        }
        // CASE album
        else if (parts[1].equals("album")) {
          String albumId = parts[2];
          Log.d(log, "Album shared: " + albumId);
          args.putString(Constants.ARG_ALBUM_ID, albumId);
          args.putSerializable(Constants.ARG_CASE, Constants.Case.Album);
        }
        // CASE track
        else if (parts[1].equals("track")) {
          String trackId = parts[2];
          Log.d(log, "Track shared: " + trackId);
          args.putString(Constants.ARG_SONG_SPOTIFY_ID, trackId);
          args.putSerializable(Constants.ARG_CASE, Constants.Case.Track);
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
