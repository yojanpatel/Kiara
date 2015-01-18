package uk.co.yojan.kiara.analysis.learning.recommendation;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.Cluster;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.server.Constants;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Uses the Q-Learning matrices to recommend the next song to be played
 */
public class BottomUpRecommender implements Recommender {

  private static final Logger log = Logger.getLogger("BottomUpRecommender");
  private static final boolean SOFT_MAX = false;

  private static final double EPSILON = 0.3;

  public double epsilon(int level, int t) {
    if(level < 2) return EPSILON / 2;
    return EPSILON;
  }

  /**
   * Bottom-up traversal starting from the LeafCluster of the currently playing track.
   *
   * @param userId     the id of the user
   * @param playlistId the id of the playlist to reccomend next track for
   * @return the spotify id of the song to play next
   */
  @Override
  public String recommend(String userId, Long playlistId) {

    // TODO: uncomment when done with experimenting
    // Playlist p = OfyUtils.loadPlaylist(userId, playlistId);
    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    LinkedList<String> history = p.history();
    int t = p.events().size();

    // Recommend song based on the last finished song.
    // If there is no previously finished song (recent), recommend based on the last song that was played
    String recentSongId = p.lastFinished();
    if(recentSongId == null) {
      recentSongId = p.previousSong();
    }

    LeafCluster currentSongLeafCluster = OfyUtils.loadLeafCluster(playlistId, recentSongId).now();
    NodeCluster parentNodeCluster = ofy().load().key(currentSongLeafCluster.getParent()).now();
    int leafClusterIndex = parentNodeCluster.clusterIndex(currentSongLeafCluster.getSongId());

    while(true) {
      // Compute the next cluster to try
      //noinspection ConstantConditions
      int nextClusterIndex = SOFT_MAX ? softMaxAction(parentNodeCluster, leafClusterIndex)
                                      : greedyEpsilon(parentNodeCluster, leafClusterIndex, t);
      Cluster nextCluster = parentNodeCluster.getChildren().get(nextClusterIndex);

      String recommendedSongId = null;

      if (nextCluster instanceof LeafCluster) {
        // recommend if not in history
        String leafSongId = ((LeafCluster) nextCluster).getSongId();
        if(!history.contains(leafSongId)) {
          recommendedSongId = leafSongId;
        }
      } else if (nextCluster instanceof NodeCluster) {
        // Use a rough cosine similarity to choose the next song from the child NodeCluster's shadow.
        recommendedSongId = selectSong((NodeCluster) nextCluster, recentSongId, history);
      }

      // recommendedSongId is not null if the prediction is valid.
      if(recommendedSongId != null) {
        return recommendedSongId;
      } else {
        // otherwise, go higher up the tree for the next iteration.
        Key<NodeCluster> parent = parentNodeCluster.getParent();
        if (parent == null) {
          log.warning("Something went wrong. No songs were recommended even after the root was reached. " + parentNodeCluster.getId());

          return parentNodeCluster.getSongIds().get(new Random().nextInt(parentNodeCluster.getChildren().size()));
        }

        // This can be done by updating the leafClusterIndex and parentNodeCluster.
        NodeCluster parentparentNodeCluster = ofy().load().key(parent).now();
        leafClusterIndex = parentparentNodeCluster.nodeClusterIndex(parentNodeCluster.getId());
        parentNodeCluster = parentparentNodeCluster;
      }
    }
  }

  /**
   *
   * @param nodeCluster NodeCluster whose shadow is the list of songs to consider
   * @param recentSongId song relative to which, a new one is to be recommended
   * @param history  a list of recently listened to songs to exclude
   *
   * @return null if no valid recommendation found for nodeCluster, otherwise the String id corresponding
   *         to the song being recommended.
   */
  private String selectSong(NodeCluster nodeCluster, String recentSongId, List<String> history) {
    SongFeature recent = OfyUtils.loadFeature(recentSongId).now();
    if(recent == null) {
      log.warning("Data not found for when using heuristic: " + recentSongId);
    }

    double maxSimilarity = Double.NEGATIVE_INFINITY;
    double minDistance = Double.POSITIVE_INFINITY;
    String currentRecommendation = null;

    Collection<SongFeature> candidates = ofy().load().keys(convertIdsToKeys(nodeCluster.getSongIds())).values();

    for (SongFeature song : candidates) {
      if(song == null) continue;
      if (!history.contains(song.getId())) {
        double sim = similarity(recent, song);
        double d = TopDownRecommender.distance(recent, song);
//        if (sim > maxSimilarity) {
        if(d < minDistance) {
          maxSimilarity = sim;
          minDistance = d;
          currentRecommendation = song.getId();
        }
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
   * Cosine similarity between end of song a and start of song b based on the the timbre vectors, tempo and loudness.
   *
   * cos(x, y) = x.y / |x||y| = sum(x_i * y_i) / sqrt(sum(x_i^2) * sum(y_i^2))
   *
   * @param a SongFeature of the currently playing song
   * @param b SongFeature of the candidate next song to play
   *
   * @return a double representing the similarity squared between two songs.
   *  -1 indicates opposites, 0 independence and 1 means exactly the same.
   */
  private Double similarity(SongFeature a, SongFeature b) {
    if(a == null || b == null) return Double.POSITIVE_INFINITY;
    if(a.getFinalTempo() == null) {
      Logger.getLogger(a.getId()).warning(a.getId());
    }
    if(b.getTempo() == null) {
      Logger.getLogger(b.getId()).warning(b.getId());
    }

    // similarity = numer / (denomA * denomB)
    double denomA = 0.0;
    double denomB = 0.0;
    double numer = 0.0;

    denomA += Math.pow(a.getFinalTempo(), 2);
    denomA += Math.pow(a.getFinalLoudness(), 2);
    denomA += Math.pow(a.getValence(), 2);
    denomA += Math.pow(a.getEnergy(), 2);

    denomB += Math.pow(b.getInitialTempo(), 2);
    denomB += Math.pow(b.getInitialLoudness(), 2);
    denomB += Math.pow(b.getValence(), 2);
    denomB += Math.pow(b.getEnergy(), 2);

    numer += a.getFinalTempo() * b.getInitialTempo();
    numer += a.getFinalLoudness() * b.getInitialLoudness();
    numer += a.getValence() * b.getValence();
    numer += a.getEnergy() * b.getEnergy();

    for(int i = 0; i < 12; i++) {
      // Mean of each timbre vector coefficient
      numer += a.getFinalTimbreMoments().get(i).get(0) * b.getFinalTimbreMoments().get(i).get(0);
      denomA += Math.pow(a.getFinalTimbreMoments().get(i).get(0), 2);
      denomB += Math.pow(b.getFinalTimbreMoments().get(i).get(0), 2);
      // Median
      numer += a.getFinalTimbreMoments().get(i).get(3) * b.getFinalTimbreMoments().get(i).get(3);
      denomA += Math.pow(a.getFinalTimbreMoments().get(i).get(3), 2);
      denomB += Math.pow(b.getFinalTimbreMoments().get(i).get(3), 2);
      // Skewness
      numer += a.getFinalTimbreMoments().get(i).get(6) * b.getFinalTimbreMoments().get(i).get(6);
      denomA += Math.pow(a.getFinalTimbreMoments().get(i).get(6), 2);
      denomB += Math.pow(b.getFinalTimbreMoments().get(i).get(6), 2);
    }

    denomA = Math.sqrt(denomA);
    denomB = Math.sqrt(denomB);

    return numer / (denomA * denomB);
  }


  /**
   *
   * SoftMax is an action selection algorithm:
   *   Prob(a) = exp(Q(s,a) / T) / Sum(exp(Q(s,i) / T))
   *   http://en.wikipedia.org/wiki/Softmax_function
   *
   * @param cluster the parent cluster for which to recommend the next clusterIndex
   * @param clusterIndex the child cluster index of the state currently in
   *
   * @return  the action to be taken, i.e. the index of the cluster in cluster to go to.
   */
  private int softMaxAction(NodeCluster cluster, int clusterIndex) {

    List<Double> stateRow = cluster.getQRow(clusterIndex);

    List<Double> actionProbabilities = new ArrayList<>();

    double denominator = 0.0;
    for(Double qVal : stateRow) {
      denominator += Math.exp(qVal / Constants.SOFTMAX_TEMPERATURE);
    }

    for(Double actionVal : stateRow) {
      double prob = Math.exp(actionVal / Constants.SOFTMAX_TEMPERATURE) / denominator;
      actionProbabilities.add(prob);
    }

    // u ~ Uniform(0,1)
    Double u = new Random().nextDouble();

    double cumulative = 0.0;
    for(int i = 0; i < actionProbabilities.size(); i++) {
      cumulative += actionProbabilities.get(i);
      if(u < cumulative) {
        return i;
      }
    }
    return -1;
  }


  /**
   * Greedy Epsilon strategy with the epsilon function.
   *
   * @param cluster the parent cluster for which to recommend the next clusterIndex
   * @param clusterIndex the child cluster index of the state currently in
   * @param t time into the learning i.e. number of prior events/tracks played etc.
   *
   * @return  the action to be taken, i.e. the index of the cluster in cluster to go to.
   */
  private int greedyEpsilon(NodeCluster cluster, int clusterIndex, int t) {
    // u ~ Uniform(0,1)
    double u = new Random().nextDouble();
    if(u < epsilon(cluster.getLevel(), t)) {
      return new Random().nextInt(cluster.getChildren().size());
    } else {

      List<Double> stateRow = cluster.getQRow(clusterIndex);

      // choose the maximising Q value
      double maxQ = Double.NEGATIVE_INFINITY;
      int minIndex = -1;

      // nextClusterIndex = argmax_i{Q(state)(i)}
      for (int i = 0; i < stateRow.size(); i++) {
        Double qValue = stateRow.get(i);
        if (maxQ < qValue) {
          minIndex = i;
          maxQ = qValue;
        }
      }
      return minIndex;
    }
  }


  /**
   * TODO: implement Max-Boltzmann Exploration method by combining greedyEpsilong and softMax,
   * probably with a higher temperature.
   */

  // Input: 2 * key + mode
  // Output: boolean if keys compatible
  private boolean harmonicMatching(int currentKey, int nextKey) {
    // c, c#, d, eb, e, f, gb, g, ab, a, bb, b
    // 0, 1 , 2, 3,  4, 5, 6,  7, 8,  9, 10, 11
    // c, g, d, a, e, b, gb, db, ab, eb, bb, f
    int key = currentKey / 2;
    int mode = currentKey % 2;

    int nkey = nextKey / 2;
    int nmode = nextKey % 2;

    int poss = 2 * ((currentKey + 7) % 12) + mode;
    int poss2 = 2 * ((currentKey - 7) % 12) + mode;
    int poss3;
    if(mode == 0) {
      poss3 = 2 * ((currentKey + 9) % 12) + 1;
    } else {
      poss3 = 2 * ((currentKey - 9) % 12);
    }

    return nextKey == poss || nextKey == poss2 || nextKey == poss3;
  }
}
