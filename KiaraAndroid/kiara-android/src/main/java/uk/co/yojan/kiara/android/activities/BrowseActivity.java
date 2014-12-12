package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.FilterTracksFragment;
import uk.co.yojan.kiara.android.fragments.PlaylistListFragment;

public class BrowseActivity extends KiaraActivity {

  Constants.Case c;
  Long playlistId;
  String spotifyPlaylistId;
  String playlistName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent trigger = getIntent();
    if(trigger != null) {
      c = (Constants.Case) trigger.getSerializableExtra(PlaylistListFragment.CASE_PARAM);
      playlistId = trigger.getLongExtra(PlaylistListFragment.PLAYLIST_ID_PARAM, 0);
      spotifyPlaylistId = trigger.getStringExtra(PlaylistListFragment.PLAYLIST_PARAM);
      playlistName = trigger.getStringExtra(PlaylistListFragment.PLAYLIST_NAME_PARAM);
    }

    FilterTracksFragment ftf = FilterTracksFragment.newInstance(spotifyPlaylistId, playlistName);
    ftf.setPlaylistId(playlistId);
    setContentView(R.layout.browse_activity);
    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.container, ftf)
          .commit();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.player, menu);
    return true;
  }
}
