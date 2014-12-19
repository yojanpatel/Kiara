package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.faradaj.blurbehind.BlurBehind;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.PlayerFragment;
import uk.co.yojan.kiara.android.parcelables.SongParcelable;
import uk.co.yojan.kiara.client.data.Song;

import java.util.List;

public class PlayerActivity extends KiaraActivity {

  long playlistId;
  SongParcelable song;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent trigger = getIntent();
    if(trigger != null) {
      song = trigger.getParcelableExtra(Constants.ARG_SONG);
      playlistId = trigger.getLongExtra(Constants.ARG_PLAYLIST_ID, -1);
    }

    setContentView(R.layout.activity_player);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (savedInstanceState == null) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, PlayerFragment.newInstance(playlistId, song))
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
        Runnable run = new Runnable() {
          @Override
          public void run() {
            String userId = sharedPreferences().getString(Constants.USER_ID, null);
            List<Song> songs = getKiaraApplication().kiaraClient().getCachedSongs(userId, playlistId);
            Intent intent = new Intent(PlayerActivity.this, QueueActivity.class);
            intent.putExtra(Constants.ARG_PLAYLIST_SONG_LIST, SongParcelable.convert(songs));
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
          }
        };
        BlurBehind.getInstance().execute(PlayerActivity.this, run);

      }
      return super.onOptionsItemSelected(item);
    }
}
