package uk.co.yojan.kiara.analysis.learning;

/**
 * Encapsulates the reward function for the Q-learning algorithm.
 *
 * Given a state (current cluster node) and an action, the reward function
 * returns a number (positive - reward; negative - punishment) to alter the
 * Q matrix by.
 */
public interface RewardFunction {

  public double rewardSkip();

  public double rewardQueue();

  public double rewardTrackFinished();

  public double rewardFavourite();
}
