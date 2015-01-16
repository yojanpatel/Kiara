package uk.co.yojan.kiara.analysis.users;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.learning.EventHistory;
import uk.co.yojan.kiara.analysis.learning.QLearner;
import uk.co.yojan.kiara.analysis.learning.Recommender;
import uk.co.yojan.kiara.analysis.learning.RewardFunction;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

import java.util.ArrayList;
import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

public abstract class HypotheticalUser {

  Playlist playlist;

  private static int LISTENING_SESSION_SIZE = 500;

  abstract String userId();
  abstract User construct();
  abstract RewardFunction rewardFunction();
  abstract Recommender recommender();

  public abstract void setRewardFunction(RewardFunction f);
  public abstract void setRecommender(Recommender r);

  // The important method that must be implemented based on the hypothetical personality, the user is following.
  abstract double behave(SongFeature current, SongFeature previous);

  private QLearner QLearner = new QLearner();

  public User user() {
    User u = OfyUtils.loadUser(userId()).now();
    if(u == null) {
      u = construct();
      ofy().save().entity(u);
    }
    return u;
  }

  // Start playing the playlist using spotifyId as the seed song.
  public Pair<Double, ArrayList<Integer>> play(String spotifyId) {
    User u = user();
    List<Playlist> playlists = new ArrayList<>(u.getAllPlaylists());
    playlist = playlists.get(0);
    return play(playlist, spotifyId);
  }

  // Start playing the playlist using spotifyId as the seed song with playlist given.
  public Pair<Double, ArrayList<Integer>> play(Playlist playlist, String spotifyId) {
    assert playlist.getAllSongIds().contains(spotifyId);
    this.playlist = playlist;
    SongFeature previous;

    start(spotifyId);
    SongFeature current = OfyUtils.loadFeature(playlist.previousSong()).now();

    String nextSongId = recommender().recommend(userId(), playlist.getId());
    SongFeature recommended = OfyUtils.loadFeature(nextSongId).now();

    finish(current.getId());
    previous = current;

    start(recommended.getId());
    current = recommended;

    ArrayList<Integer> skips = new ArrayList<>();
    double reward = 0.0;

    // Repeat for LISTENING_SESSION_SIZE episodes
    for(int i = 0; i < LISTENING_SESSION_SIZE; i++) {

      // load the next recommendation
      nextSongId = recommender().recommend(userId(), playlist.getId());
      recommended = OfyUtils.loadFeature(nextSongId).now();

      // Behave based on the hypothetical users personality.
      // Must perform either skip() or finish()
      // e.g. BeatLover skips if current and previous' tempo are not similar enough.

      double r = behave(current, previous);
      if(r < 0) {
        skips.add(i);
      }

      reward += r;

      // prepare for next iter
      previous = current;
      start(recommended.getId());
      current = recommended;
    }
    reward /= LISTENING_SESSION_SIZE;
    return new Pair<>(reward, skips);
  }



  protected void start(String id) {
    playlist.nowPlaying(id); // async
    ofy().save().entity(playlist);
  }

  protected void finish(String id) {

    double r = rewardFunction().rewardTrackFinished();

    if(playlist.lastFinished() != null) {
      // record event in the events history for playlist
      EventHistory.addEnd(playlist.events(), playlist.lastFinished(), id);

      if (playlist.lastFinished() != null) {
        LeafCluster previous = OfyUtils.loadLeafCluster(playlist.getId(), playlist.lastFinished()).now();
        LeafCluster current = OfyUtils.loadLeafCluster(playlist.getId(), id).now();
        QLearner.update(previous, current, r);
      }
    }

    // update the last successful song played in the playlist
    playlist.justFinished(id);//.now();
    ofy().save().entities(playlist).now();
  }

  protected void skip(String id, int percent) {
    double r = rewardFunction().rewardSkip(percent);
    if(playlist.lastFinished() != null) {
      // record event in the events history for playlist
      EventHistory.addSkipped(playlist.events(), playlist.lastFinished(), id, percent);
      ofy().save().entity(playlist).now(); // async
      LeafCluster previous = OfyUtils.loadLeafCluster(playlist.getId(), playlist.lastFinished()).now();
      LeafCluster current = OfyUtils.loadLeafCluster(playlist.getId(), id).now();
      QLearner.update(previous, current, r);
    }

    if(percent > 80) playlist.justFinished(id);
    ofy().save().entities(playlist).now();
  }


  protected void favourite(String songId) {
    double r = rewardFunction().rewardFavourite();

    if(playlist.lastFinished() != null) {
      // record event in the events history for playlist
      EventHistory.addFavourite(playlist.events(), playlist.lastFinished(), songId);
      ofy().save().entity(playlist).now(); // async

      LeafCluster previous = OfyUtils.loadLeafCluster(playlist.getId(), playlist.lastFinished()).now();
      LeafCluster current = OfyUtils.loadLeafCluster(playlist.getId(), songId).now();

      QLearner.update(previous, current, r);
    }
  }

  public void setQLearner(QLearner QLearner) {
    this.QLearner = QLearner;
  }
}
