package uk.co.yojan.kiara.analysis.learning;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Uses the Q-Learning matrices to recommend the next song to be played
 */
public class LearnedRecommender implements Recommender {

  private static final Logger log = Logger.getLogger("LearnedRecommender");


  private static final double EPSILON = 0.3;

  private static double epsilon(int level) {
    if(level < 2) return 0;
    return EPSILON;
  }

  /**
   * @param userId     the id of the user
   * @param playlistId the id of the playlist to reccomend next track for
   * @return the spotify id of the song to play next
   */
  @Override
  public String recommend(String userId, Long playlistId) {
    Playlist p = OfyUtils.loadPlaylist(userId, playlistId);

    LinkedList<String> history = p.history();
    String recentSongId = p.previousSong();

    // start at the root node
    NodeCluster current = OfyUtils.loadRootCluster(playlistId).now();

    // child index for the cluster that contained the song just played
    int clusterIndex = current.clusterIndex(recentSongId);
    while (clusterIndex >= 0) {
      // choose the Q-chosen cluster with probability p = 1 - epsilon(level)
      List<List<Double>> Q = current.getQ();
      List<Double> stateRow = Q.get(clusterIndex);

      double epsilonProb = new Random().nextDouble();
      int nextClusterIndex = -1;

      // with probability epsilon:
      //   choose random cluster based on Q distribution
      if (epsilonProb < epsilon(current.getLevel())) {
        log.warning("Probabilistic Choice based on Distribution of Q");
        double sum = 0;
        for (Double qValue : stateRow) sum += qValue;
        Double clusterProb = new Random().nextDouble() * sum;
        double cumulative = 0;
        for (int i = 0; i < stateRow.size(); i++) {
          Double qValue = stateRow.get(i);
          cumulative += qValue;
          if (cumulative > clusterProb) {
            nextClusterIndex = i;
            break;
          }
        }
      } else {
        log.warning("Using policy recommendation at this level " + stateRow.size());
        // choose the maximising Q value
        double maxQ = Double.NEGATIVE_INFINITY;

        // nextClusterIndex = argmax_i{Q(state)(i)}
        for (int i = 0; i < stateRow.size(); i++) {
          Double qValue = stateRow.get(i);
          if (maxQ < qValue) {
            nextClusterIndex = i;
            maxQ = qValue;
          }
        }
      }

      String nextClusterId = current.getChildIds().get(nextClusterIndex);
      log.warning("nextClusterId " + nextClusterId);
      // if the next cluster is a LeafCluster, recommend if not already played
      if (current.containsLeaf(nextClusterId)) {
        LeafCluster nextLeaf = OfyUtils.loadLeafCluster(nextClusterId).now();
        log.warning(nextLeaf.getSongId());
        // String id = nextClusterId.split("-")[1];
        if (!history.contains(nextLeaf.getSongId())) {
          log.warning("A leaf was recommended, was not found in history: " + nextLeaf.getSongId());
          return nextLeaf.getSongId();
        } else {
          // break from while loop and recommend using heuristic from current
          // ALTERNATIVE: choose second highest Q value
          break;
        }
      } else {
        // update loop increment variables
        // if the next cluster is a NodeCluster
        current = OfyUtils.loadNodeCluster(nextClusterId).now();

        // -1 if source is from a different branch of the tree, will break from while loop.
        clusterIndex = current.clusterIndex(recentSongId);
      }
    }
    // assert: current is a NodeCluster
    // assert: either clusterIndex == -1 (different path from source song) or recommended song has been played recently
    return selectSong(current, recentSongId, history);
  }


  private String selectSong(NodeCluster nodeCluster, String recentSongId, List<String> history) {
    SongFeature recent = OfyUtils.loadFeature(recentSongId).now();
    NodeCluster current = nodeCluster;
    Collection<SongFeature> candidates;

    // TODO: base this off distance(end_recent, begin_candidate[i])
    log.warning("Heuristic choice");

    // for now, closest based on tempo.
    double tempoDifference = Double.MAX_VALUE;
    double minDistance = Double.MAX_VALUE;

    String currentRecommendation = null;

    while(currentRecommendation == null) {
      candidates = ofy().load().keys(convertIdsToKeys(current.getSongIds())).values();

      for (SongFeature song : candidates) {
        if (!history.contains(song.getId())) {
//          double diff = Math.abs(song.getTempo() - recent.getTempo());
          double diff = distance(recent, song);
//          if (diff < tempoDifference) {
          if (diff < minDistance) {
            minDistance = diff;
            currentRecommendation = song.getId();
          }
        }
      }

      if(currentRecommendation == null) {
        current = ofy().load().key(current.getParent()).now();
      }
    }

    return currentRecommendation;
  }

  private List<Key<SongFeature>> convertIdsToKeys(List<String> ids) {
    ArrayList<Key<SongFeature>> keys = new ArrayList<>();
    for(String id : ids) keys.add(Key.create(SongFeature.class, id));
    return keys;
  }

  /**
   * Distance between end of song a and start of song b based on the the timbre vectors, tempo and loudness.
   * @return
   */
  private Double distance(SongFeature a, SongFeature b) {
    double d = 0.0;
    d += Math.pow(a.getFinalTempo() - b.getInitialTempo(), 2);
    d += Math.pow(a.getFinalLoudness() - b.getInitialLoudness(), 2);


    for(int i = 0; i < 12; i++) {
      // Mean of each timbre vector coefficient
      d += Math.pow(a.getFinalTimbreMoments().get(i).get(0) - b.getFinalTimbreMoments().get(i).get(0), 2);
      // Median
      d += Math.pow(a.getFinalTimbreMoments().get(i).get(3) - b.getFinalTimbreMoments().get(i).get(3), 2);
      // Skewness
      d += Math.pow(a.getFinalTimbreMoments().get(i).get(6) - b.getFinalTimbreMoments().get(i).get(6), 2);
    }

    return d;
  }
}
