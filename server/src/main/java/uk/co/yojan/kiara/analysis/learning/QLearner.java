package uk.co.yojan.kiara.analysis.learning;

import uk.co.yojan.kiara.analysis.cluster.NodeCluster;

import java.util.List;

/**
 * Created by yojan on 12/14/14.
 */
public class QLearner {

  // Learning rate
  private static double alpha() {
    return 0.5;
  }

  // Discount factor
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
   * @return  the updated Q(s, a) value
   */
  public static double learn(NodeCluster cluster, int stateIndex, int actionIndex, double reward) {
    List<Double> stateRow = cluster.getQ().get(stateIndex);
    List<Double> actionRow = cluster.getQ().get(actionIndex);

    // Q learning
    double maxQ = Double.MIN_VALUE;
    for(Double d : actionRow) {
      maxQ = Math.max(maxQ, d);
    }

    double updatedQ = (1 - alpha()) * stateRow.get(actionIndex) + alpha() * (reward + gamma() * maxQ);

    stateRow.set(actionIndex, updatedQ);

    return updatedQ;
  }
}
