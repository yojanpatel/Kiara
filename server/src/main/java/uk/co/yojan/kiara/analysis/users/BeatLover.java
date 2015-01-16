package uk.co.yojan.kiara.analysis.users;

import uk.co.yojan.kiara.analysis.learning.LearnedRecommender;
import uk.co.yojan.kiara.analysis.learning.Recommender;
import uk.co.yojan.kiara.analysis.learning.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.VariedSkipReward;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

import java.util.logging.Logger;

/**
 * Hypothetical User for evaluation.
 *
 * Behaves predictably in the following manner:
 *  -- Skip song N if its tempo is more than a certain threshold away from song N-1.
 *
 *
 * By listening to various playlists over many iterations, the Q matrices will be computed, and reach a final state.
 */
public class BeatLover extends HypotheticalUser {

  private static RewardFunction rewardFunction = new VariedSkipReward();
  private static Recommender recommender = new LearnedRecommender();

  private static int TEMPO_THRESHOLD = 8;

  @Override
  double behave(SongFeature current, SongFeature previous) {
    double reward = 0.0;

    double tempoDiff = Math.abs(current.getTempo() - previous.getTempo());
    if(tempoDiff < TEMPO_THRESHOLD) {
      if(tempoDiff < TEMPO_THRESHOLD / 2) {
        favourite(current.getId());
        reward += rewardFunction.rewardFavourite();
      }
      finish(current.getId());
      reward += rewardFunction.rewardTrackFinished();
    } else {
      Logger.getLogger("").info("Skipping track as BeatLover (" + current.getTempo() + " - " + previous.getTempo() + ")");
      int percent;
      if(tempoDiff < 2 * TEMPO_THRESHOLD)
        percent = 60;
      else if(tempoDiff < 4 * TEMPO_THRESHOLD)
        percent = 30;
      else
        percent = 15;
      skip(current.getId(), percent);
      reward += rewardFunction.rewardSkip(percent);
    }

    return reward;
  }

  @Override
  User construct() {
    User user = new User();
    user.setFirstName("Beat");
    user.setLastName("Lover");
    user.setId(userId());
    return user;
  }

  @Override
  public String userId() {
    return "beatlover-hypothetical";
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
