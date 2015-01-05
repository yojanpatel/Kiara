package uk.co.yojan.kiara.analysis.users;

import uk.co.yojan.kiara.analysis.learning.LearnedRecommender;
import uk.co.yojan.kiara.analysis.learning.Recommender;
import uk.co.yojan.kiara.analysis.learning.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.VariedSkipReward;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

import java.util.Random;

/**
 * Erratic Earl behaves erratically.
 *
 * Skips, Finishes, Favourites randomly.
 */
public class ErraticEarl extends HypotheticalUser {

  private static RewardFunction rewardFunction = new VariedSkipReward();
  private static Recommender recommender = new LearnedRecommender();

  @Override
  void behave(SongFeature current, SongFeature previous) {
    double p = new Random().nextDouble();

    // P(favourite) = 0.5
    if(p < 0.5) {
      favourite(current.getId());
    }

    // P(skip) = 0.5 and P(finish) = 0.5
    // skip percents uniformly distributed
    p = new Random().nextDouble();
    if(p < 0.5) {
      skip(current.getId(), new Random().nextInt(100));
    } else {
      finish(current.getId());
    }
  }

  @Override
  String userId() {
    return "erratic-earl-hypothetical";
  }

  @Override
  User construct() {
    User u = new User();
    u.setFirstName("Erratic");
    u.setLastName("Earl");
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
