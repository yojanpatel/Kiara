package uk.co.yojan.kiara.analysis.learning;

import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;

import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Created by yojan on 12/14/14.
 */
public class QLearner {

  private static double BASE_ALPHA = 0.5;

  // Learning rate, decreases with leaf distance with the node
  private static double alpha(int from, int to, int node) {
    int closestLevel = Math.min(from, to);
    return Math.pow(BASE_ALPHA, node - closestLevel);
  }

  // Discount factor [0,1]
  // 0 - myopic, short-sighted
  // 1 - long-term high reward
  private static double gamma() {
    return 0.5;
  }

  /**
   *
   * @param cluster  the parent cluster whose Q-matrix is being updated
   * @param stateIndex  the index into the Q matrix to find the cluster, the previous song was in.
   *                    i.e. the previous cluster, s_t
   * @param actionIndex  the index into the state's row to locate the cluster transitioned to.
   *                     i.e. the current cluster, s_t+1
   * @param reward  the reward emitted due to the transition.
   * @param fromLevel the level at which the 'from' LeafCluster is located for alpha calculations.
   * @param toLevel the level at which the 'to' LeafCluster is located for alpha calculations.
   * @return  the updated Q(s, a) value
   */
  public static double learn(NodeCluster cluster, int stateIndex, int actionIndex, double reward, int fromLevel, int toLevel) {
    List<Double> stateRow = cluster.getQ().get(stateIndex);
    List<Double> actionRow = cluster.getQ().get(actionIndex);

    // Q learning
    double maxQ = Double.MIN_VALUE;
    for(Double d : actionRow) {
      maxQ = Math.max(maxQ, d);
    }

    double clusterAlpha = alpha(fromLevel, toLevel, cluster.getLevel());

    double updatedQ = (1 - clusterAlpha) * stateRow.get(actionIndex) + clusterAlpha * (reward + gamma() * maxQ);

    stateRow.set(actionIndex, updatedQ);

    ofy().save().entity(cluster).now();

    return updatedQ;
  }


  /**
   * The main method responsible for updating the Q-learning matrices along the hierarchical clusters.
   *
   * Traverse up the hierarchy starting from the leaf clusters, updating the relevant lowest common ancestor
   * at the value represented by the child cluster indices (previousSongCluster, currentSongCluster).
   *
   * All clusters higher up the tree, until the root are updated along the diagonal.
   *
   *
   * @param previousSong the song that was played previously, from the history of the playlist
   * @param currentSong the song on what the action was carried out
   * @param reward  the reward emitted from the reward function
   */
  public static void update(LeafCluster previousSong, LeafCluster currentSong, double reward) {

    // LCA is the first NodeCluster going towards the root (i.e. following parent links)
    // such that the shadow contains both the previous song id, and the current song id.
    NodeCluster ancestor = ofy().load().key(previousSong.getParent()).now();
    while(!commonAncestor(ancestor, previousSong.getSongId(), currentSong.getSongId())) {
      ancestor = ofy().load().key(ancestor.getParent()).now();
    }

    // assert: ancestor is the lowest common ancestor of cluster nodes previousSong and currentSong

    learn(
        ancestor,
        ancestor.clusterIndex(previousSong.getSongId()),
        ancestor.clusterIndex(currentSong.getSongId()),
        reward,
        previousSong.getLevel(),
        currentSong.getLevel());


    // traverse up the tree til the root, and update the diagonal values.
    while(ancestor.getParent() != null) {

      ancestor = ofy().load().key(ancestor.getParent()).now();

      // assert: clusterIndex(previousSong) == clusterIndex(currentSong)

      learn(
          ancestor,
          ancestor.clusterIndex(previousSong.getSongId()),
          ancestor.clusterIndex(currentSong.getSongId()),
          reward,
          previousSong.getLevel(),
          currentSong.getLevel());
    }
  }

  /**
   *
   * @param node candidate node being tested for being the common ancestor.
   * @param songIdA the song id for the first song.
   * @param songIdB the song id for the second song.
   * @return true if the songs represented by songIdA and songIdB have node as a common ancestor.
   */
  public static boolean commonAncestor(NodeCluster node, String songIdA, String songIdB) {
    return node.getSongIds().contains(songIdA) && node.getSongIds().contains(songIdB);
  }
}
