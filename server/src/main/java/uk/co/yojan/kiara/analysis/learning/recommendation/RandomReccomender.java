package uk.co.yojan.kiara.analysis.learning.recommendation;

import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.server.models.Playlist;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class RandomReccomender implements Recommender {
  /**
   * @param userId     the id of the user
   * @param playlistId the id of the playlist to reccomend next track for
   * @return the spotify id of the song to play next
   */
  @Override
  public String recommend(String userId, Long playlistId) {
    Playlist p = OfyUtils.loadPlaylist(userId, playlistId);

    List<String> songs = new ArrayList<>(p.getAllSongIds());

    LinkedList<String> history = p.history();

    String reccomendedSongId = songs.get((int) (Math.random() * songs.size()));
    while(history.contains(reccomendedSongId)) {
      reccomendedSongId = songs.get((int) (Math.random() * songs.size()));
    }

    return reccomendedSongId;
  }
}
