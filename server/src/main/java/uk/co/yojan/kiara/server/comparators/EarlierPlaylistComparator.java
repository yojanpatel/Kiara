package uk.co.yojan.kiara.server.comparators;


import uk.co.yojan.kiara.server.models.Playlist;

import java.util.Comparator;

/**
 * Comparator for ordering two Playlist's last viewed time.
 */
public class EarlierPlaylistComparator implements Comparator<Playlist> {
  @Override
  public int compare(Playlist a, Playlist b) {
    if(a.getLastViewedTimestamp() < b.getLastViewedTimestamp())
      return -1;
    else if(a.getLastViewedTimestamp() > b.getLastViewedTimestamp())
      return +1;
    else
      return 0;
  }
}
