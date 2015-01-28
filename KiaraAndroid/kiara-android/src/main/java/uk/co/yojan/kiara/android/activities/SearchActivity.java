package uk.co.yojan.kiara.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import uk.co.yojan.kiara.android.Constants;
import uk.co.yojan.kiara.android.R;
import uk.co.yojan.kiara.android.fragments.PlayerControlFragment;
import uk.co.yojan.kiara.android.fragments.SearchFragment;

public class SearchActivity extends KiaraActivity {

    private static final String log = "SearchActivity";

    private long playlistId;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mToolbar = getToolbar();
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // initialise playlist param for fragment from triggering intent
        Intent trigger = getIntent();
        playlistId = trigger.getLongExtra(Constants.ARG_PLAYLIST_ID, -1);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                .add(R.id.container, SearchFragment.newInstance(playlistId))
                .commit();
        }

        if(sharedPreferences().getBoolean(Constants.IN_SESSION, false)) {
            Log.d(log, "adding control fragment to activity.");
            getFragmentManager().beginTransaction()
                .add(R.id.controller_container, PlayerControlFragment.newInstance()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
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
