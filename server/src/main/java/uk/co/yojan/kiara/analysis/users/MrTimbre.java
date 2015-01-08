package uk.co.yojan.kiara.analysis.users;

import uk.co.yojan.kiara.analysis.learning.LearnedRecommender;
import uk.co.yojan.kiara.analysis.learning.Recommender;
import uk.co.yojan.kiara.analysis.learning.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.VariedSkipReward;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

/**
 * Mr.Timbre is a hypothetical user that
 */
public class MrTimbre extends HypotheticalUser {

  private static RewardFunction rewardFunction = new VariedSkipReward();
  private static Recommender recommender = new LearnedRecommender();

  @Override
  boolean behave(SongFeature current, SongFeature previous) {
    return false;
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
}
