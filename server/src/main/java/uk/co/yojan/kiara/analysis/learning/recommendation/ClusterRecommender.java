package uk.co.yojan.kiara.analysis.learning.recommendation;

import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.server.models.Playlist;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Created by yojan on 12/19/14.
 */
public class ClusterRecommender implements Recommender {

  private static final int PERIMETER_SIZE = 4;

  /**
   * @param userId   the id of the user
   * @param playlist the playlist to reccomend next track for
   * @return the spotify id of the song to play next
   */
  @Override
  public String recommend(String userId, Playlist playlist, String songId) {
    LinkedList<String> history = playlist.history();
    String recentSongId = playlist.previousSong();
    ArrayList<String> perimeter = new ArrayList<>();

    LeafCluster leaf = OfyUtils.loadLeafCluster(playlist.getId(), recentSongId).now();
    NodeCluster parent = ofy().load().key(leaf.getParent()).now();

    if(parent != null) {
      while (perimeter.size() < PERIMETER_SIZE) {

        List<String> shadow = parent.getSongIds();
        for (String id : shadow) {
          if (perimeter.size() >= PERIMETER_SIZE) break;

          if (!history.contains(id) && !perimeter.contains(id)) {
            perimeter.add(id);
          }
        }

        if (parent.getParent() != null)
          parent = ofy().load().key(parent.getParent()).now();
        else
          break;
      }
    } else {
      return null;
    }

    return perimeter.get((int) (Math.random() * perimeter.size()));
  }
}
