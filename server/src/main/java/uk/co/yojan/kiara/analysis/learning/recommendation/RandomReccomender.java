package uk.co.yojan.kiara.analysis.learning.recommendation;

import uk.co.yojan.kiara.server.models.Playlist;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class RandomReccomender implements Recommender {
  /**
   * @param userId     the id of the user
   * @param playlist the playlist to reccomend next track for
   * @return the spotify id of the song to play next
   */
  @Override
  public String recommend(String userId, Playlist playlist, String songId) {
    List<String> songs = new ArrayList<>(playlist.getAllSongIds());

    LinkedList<String> history = playlist.history();

    String reccomendedSongId = songs.get((int) (Math.random() * songs.size()));
    while(history.contains(reccomendedSongId)) {
      reccomendedSongId = songs.get((int) (Math.random() * songs.size()));
    }

    return reccomendedSongId;
  }
}
