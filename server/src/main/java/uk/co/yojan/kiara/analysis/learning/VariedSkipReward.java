package uk.co.yojan.kiara.analysis.learning;

/**
 * Created by yojan on 1/3/15.
 */
public class VariedSkipReward implements RewardFunction {
  @Override
  public double rewardSkip(int percent) {
    return percent < 10 ? -20 :
           percent < 20 ? -15 :
           percent < 50 ? -10 :
           percent < 75 ? 0 :
           percent < 90 ? 10 :
           rewardTrackFinished();
  }

  @Override
  public double rewardQueue() {
    return 15;
  }

  @Override
  public double rewardTrackFinished() {
    return 15;
  }

  @Override
  public double rewardFavourite() {
    return 20;
  }
}
