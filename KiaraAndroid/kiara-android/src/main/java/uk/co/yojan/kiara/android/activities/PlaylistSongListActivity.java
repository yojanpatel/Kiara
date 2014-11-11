package uk.co.yojan.kiara.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.SongListFragment;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;

import java.util.ArrayList;

public class PlaylistSongListActivity extends KiaraActivity
    implements SongListFragment.OnFragmentInteractionListener {

    private static final String log = PlaylistSongListActivity.class.getName();

    public static final String SONG_LIST_ARG_KEY = "SONG_LIST_KEY";
    public static final String PLAYLIST_ID_ARG_KEY = "PLAYLIST_ID_ARG_KEY";

    private ArrayList<SongParcelable> songs;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Intent trigger = getIntent();
      if(trigger != null) {
        songs = trigger.getParcelableArrayListExtra(SONG_LIST_ARG_KEY);
        id = trigger.getLongExtra(PLAYLIST_ID_ARG_KEY, -1);
      }

      setContentView(R.layout.activity_playlist_song_list);
      if (savedInstanceState == null) {
          getFragmentManager().beginTransaction()
                  .add(R.id.container, SongListFragment.newInstance(id, songs))
                  .commit();
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
