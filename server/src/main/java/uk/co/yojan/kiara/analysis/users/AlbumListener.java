package uk.co.yojan.kiara.analysis.users;

import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.learning.recommendation.Recommender;
import uk.co.yojan.kiara.analysis.learning.recommendation.TopDownRecommender;
import uk.co.yojan.kiara.analysis.learning.rewards.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.rewards.VariedSkipReward;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

import java.util.Random;

/**
 * Created by yojan on 1/26/15.
 */
public class AlbumListener extends HypotheticalUser {

  private static RewardFunction rewardFunction = new VariedSkipReward();
  private static Recommender recommender = new TopDownRecommender();

  @Override
  String userId() {
    return "albumlistener-hypothetical";
  }

  @Override
  User construct() {
    User user = new User();
    user.setFirstName("Album");
    user.setLastName("Listener");
    user.setId(userId());
    return user;
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

  @Override
  double behave(SongFeature current, SongFeature previous) {
    Result<Song> s1 = OfyUtils.loadSong(current.getId());
    Song prev = OfyUtils.loadSong(previous.getId()).now();
    Song curr = s1.now();
    double reward = 0.0;

    if(prev.getAlbumName().equals(curr.getAlbumName())) {

      if(new Random().nextDouble() < 0.5) {
        reward += rewardFunction.rewardFavourite();
        favourite(current.getId());
      }
      reward += rewardFunction.rewardTrackFinished();
      finish(current.getId());
    } else {
      int percent = new Random().nextInt(60);
      skip(current.getId(), percent);
      reward += rewardFunction.rewardSkip(percent);
    }
    return reward;
  }
}
