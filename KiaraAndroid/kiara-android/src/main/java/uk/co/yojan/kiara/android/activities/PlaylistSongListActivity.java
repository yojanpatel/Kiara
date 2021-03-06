package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.PlayerControlFragment;
import uk.co.yojan.kiara.android.fragments.SongListFragment;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;

import java.util.ArrayList;

public class PlaylistSongListActivity extends KiaraActivity
    implements SongListFragment.OnFragmentInteractionListener {

    private static final String log = PlaylistSongListActivity.class.getName();

//    public static final String SONG_LIST_ARG_KEY = "SONG_LIST_KEY";
//    public static final String PLAYLIST_ID_ARG_KEY = "PLAYLIST_ID_ARG_KEY";
//    public static final String PLAYLIST_NAME_ARG_KEY = "PLAYLIST_NAME_ARG_KEY";

    private ArrayList<SongParcelable> songs;
    private long id;
    private String playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Intent trigger = getIntent();
      if(trigger != null) {
        songs = trigger.getParcelableArrayListExtra(Constants.ARG_PLAYLIST_SONG_LIST);
        id = trigger.getLongExtra(Constants.ARG_PLAYLIST_ID, -1);
        playlistName = trigger.getStringExtra(Constants.ARG_PLAYLIST_NAME);
        Log.d(log, playlistName);
      }

      setContentView(R.layout.activity_playlist_song_list);
      getSupportActionBar().setDisplayShowTitleEnabled(false);

      if (savedInstanceState == null) {
          getFragmentManager().beginTransaction()
                  .add(R.id.container, SongListFragment.newInstance(id, playlistName, songs))
                  .commit();
      }
      addIndeterminateProgressBar();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.playlist_song_list, menu);
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

  @Override
  public void onFragmentInteraction(Uri uri) {
    Log.d(log, uri.toString());
  }
}
