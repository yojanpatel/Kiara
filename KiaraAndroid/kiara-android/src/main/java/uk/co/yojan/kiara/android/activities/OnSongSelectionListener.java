package uk.co.yojan.kiara.android.activities;


import uk.co.yojan.kiara.client.data.spotify.Track;

import java.util.HashSet;

/**
 * Created by yojan on 2/2/15.
 */
public interface OnSongSelectionListener {

  public void onSongSelectionChanged(HashSet<Track> tracks);
}
