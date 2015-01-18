package uk.co.yojan.kiara.analysis.learning.rewards;

public class VariedSkipReward implements RewardFunction {
  @Override
  public double rewardSkip(int percent) {
    return percent < 10 ? -1 :
           percent < 20 ? -0.75 :
           percent < 50 ? -0.5 :
           percent < 75 ? 0 :
           percent < 90 ? 0.5 :
           rewardTrackFinished();
  }

  @Override
  public double rewardQueue() {
    return 0.75;
  }

  @Override
  public double rewardTrackFinished() {
    return 0.75;
  }

  @Override
  public double rewardFavourite() {
    return 1;
  }
}
