package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.faradaj.blurbehind.BlurBehind;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.QueueFragment;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;

import java.util.ArrayList;

public class QueueActivity extends KiaraActivity {

  private long playlistId;
  private ArrayList<SongParcelable> songs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_queue);

    BlurBehind.getInstance()
        .withAlpha(80)
        .withFilterColor(R.color.grey400)
        .setBackground(this);

    Intent trigger = getIntent();
    if(trigger != null) {
      songs = trigger.getParcelableArrayListExtra(Constants.ARG_PLAYLIST_SONG_LIST);
      playlistId = trigger.getLongExtra(Constants.ARG_PLAYLIST_ID, -1);
    }

    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.container, QueueFragment.newInstance(playlistId, songs))
          .commit();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_queue, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
