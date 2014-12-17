package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.FilterTracksFragment;

public class BrowseActivity extends KiaraActivity {

  Constants.Case c;
  // Kiara
  Long playlistId;
  String playlistName;
  // Spotify
  String spotifyPlaylistId;
  String spotifyplaylistName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent trigger = getIntent();
    if(trigger != null) {
      c = (Constants.Case) trigger.getSerializableExtra(Constants.ARG_CASE);
      playlistId = trigger.getLongExtra(Constants.ARG_PLAYLIST_ID, 0);
      spotifyPlaylistId = trigger.getStringExtra(Constants.ARG_PLAYLIST_SPOTIFY_ID);
      spotifyplaylistName = trigger.getStringExtra(Constants.ARG_PLAYLIST_SPOTIFY_NAME);
      playlistName = trigger.getStringExtra(Constants.ARG_PLAYLIST_NAME);
    }

    FilterTracksFragment ftf = FilterTracksFragment.newInstance(spotifyPlaylistId, spotifyplaylistName);
    ftf.setPlaylistId(playlistId);
    ftf.setPlaylistName(playlistName);
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
