package uk.co.yojan.kiara.analysis.learning.rewards;

/**
 * Reward Function limited to the range [-1, 1] with functional smoothing for skips.
 */
public class UnityReward implements RewardFunction {

  @Override
  public double rewardSkip(int percent) {
    // first/last 5 percent do not count for the cosine smoothing.
    if(percent < 5) {
      return 0.8;
    } else if(percent < 95) {
      return 0.8 * Math.cos(Math.PI * (percent - 90.0) / 90.0);
    } else {
      return +0.8;
    }
  }

  @Override
  public double rewardQueue() {
    return 0.8;
  }

  @Override
  public double rewardTrackFinished() {
    return 0.8;
  }

  @Override
  public double rewardFavourite() {
    return 1.0;
  }
}
