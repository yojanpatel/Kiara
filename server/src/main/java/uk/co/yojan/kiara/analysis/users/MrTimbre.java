package uk.co.yojan.kiara.analysis.users;

import uk.co.yojan.kiara.analysis.features.SimilarityMatrix;
import uk.co.yojan.kiara.analysis.learning.recommendation.Recommender;
import uk.co.yojan.kiara.analysis.learning.recommendation.TopDownRecommender;
import uk.co.yojan.kiara.analysis.learning.rewards.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.rewards.VariedSkipReward;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Mr.Timbre is a hypothetical user that
 */
public class MrTimbre extends HypotheticalUser {

  private static RewardFunction rewardFunction = new VariedSkipReward();
  private static Recommender recommender = new TopDownRecommender();

  SimilarityMatrix similarityMatrix;

  public MrTimbre() {}

  public MrTimbre(Playlist p) {
    similarityMatrix = new SimilarityMatrix(p);
  }

  @Override
  double behave(SongFeature current, SongFeature previous) {

    double reward = 0.0;

    try {
      assert similarityMatrix != null;
      assert current != null;
      assert previous != null;

      Double similarity = similarityMatrix.get(current.getId(), previous.getId());
      if(similarity == null) {
        Logger.getLogger("").warning("Similarity null for " + current.getId() + ", " + previous.getId());
        skip(current.getId(), 0);
        return rewardFunction.rewardSkip(0);
      }
      LinkedList<String> history = playlist.history();
      HashMap<String, Double> similarityRow = similarityMatrix.get().get(previous.getId());
      ArrayList<Double> other = new ArrayList<>();
      for(String key : similarityRow.keySet()) {
        // exclude songs that cannot be chosen in this heuristic as it is unfair
        if(!history.contains(key)) {
          other.add(similarityRow.get(key));
        }
      }
      other.add(similarity);
      Collections.sort(other);

      double proportion = other.indexOf(similarity) / (double) other.size();
      if(proportion < 0.2) {
        if(proportion < 0.05) {
          favourite(current.getId());
          reward += rewardFunction.rewardFavourite();
        }
        finish(current.getId());
        reward += rewardFunction.rewardTrackFinished();
      } else {
        int percent = 100 - (int)(100 * (proportion - 0.2) / 0.8);
        skip(current.getId(), percent);
        reward += rewardFunction.rewardSkip(percent);
      }
      return reward;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

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
