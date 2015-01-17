package uk.co.yojan.kiara.analysis.users;

import uk.co.yojan.kiara.analysis.learning.recommendation.TopDownRecommender;
import uk.co.yojan.kiara.analysis.learning.recommendation.Recommender;
import uk.co.yojan.kiara.analysis.learning.rewards.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.rewards.VariedSkipReward;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

/**
 * Mr.Timbre is a hypothetical user that
 */
public class MrTimbre extends HypotheticalUser {

  private static RewardFunction rewardFunction = new VariedSkipReward();
  private static Recommender recommender = new TopDownRecommender();

  @Override
  double behave(SongFeature current, SongFeature previous) {
    return 1.0;
  }

  @Override
  public String userId() {
    return "mister-timbre-hypothetical";
  }

  @Override
  User construct() {
    User u = new User();
    u.setFirstName("Mister");
    u.setLastName("Timbre");
    u.setId(userId());
    return u;
  }

  @Override
  RewardFunction rewardFunction() {
    return rewardFunction;
  }

  @Override
  Recommender recommender() {
    return recommender;
  }

  @Override
  public void setRewardFunction(RewardFunction f) {
    rewardFunction = f;
  }

  @Override
  public void setRecommender(Recommender r) {
    recommender = r;
  }
}
