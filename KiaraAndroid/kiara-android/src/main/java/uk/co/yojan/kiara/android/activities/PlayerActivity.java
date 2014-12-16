package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.PlayerFragment;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;

public class PlayerActivity extends KiaraActivity {

  SongParcelable song;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent trigger = getIntent();
    if(trigger != null) {
      song = trigger.getParcelableExtra(PlayerFragment.SONG_PARAM);
    }

    setContentView(R.layout.activity_player);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (savedInstanceState == null) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, PlayerFragment.newInstance(song))
                .commit();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.player, menu);
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
      } else if (id == R.id.action_queue) {
        // TODO open queue activity, with dynamic search.
        toast("QUEUE");
      }
      return super.onOptionsItemSelected(item);
    }
}
