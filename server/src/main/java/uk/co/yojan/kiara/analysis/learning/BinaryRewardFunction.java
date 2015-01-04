package uk.co.yojan.kiara.analysis.learning;

/**
 * Binary reward function classifies each action as either good or bad.
 *
 * +1 is awarded for all positive actions, -1 for negative.
 */
public class BinaryRewardFunction implements RewardFunction {

  @Override
  public double rewardSkip(int percent) {
    return -1;
  }

  @Override
  public double rewardQueue() {
    return 1; // technically 0, but -1 for symmetry.
  }

  @Override
  public double rewardTrackFinished() {
    return 1;
  }

  @Override
  public double rewardFavourite() {
    return 1;
  }
}
