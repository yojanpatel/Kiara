package uk.co.yojan.kiara.analysis.research;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;
import uk.co.yojan.kiara.analysis.cluster.KMeans;
import uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer;
import uk.co.yojan.kiara.analysis.learning.QLearner;
import uk.co.yojan.kiara.analysis.learning.recommendation.Recommender;
import uk.co.yojan.kiara.analysis.learning.rewards.RewardFunction;
import uk.co.yojan.kiara.analysis.users.HypotheticalUser;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.Instances;

import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer.featureKeys;
import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Dual purpose Experiment entity class.
 * used to try various feature weights for clustering evaluation and,
 * try various learning parameters such as alpha, gamma, epsilon, strategies etc.
 */
@Entity
public class Experiment {

  @Id private String id;

  // number of clusters
  private int K;
  // playlist map: the spotify curated playlists
  @Serialize(zip=true) LinkedHashMap<String, Integer> playlistMap;
  // score map: <feature weights, K> used to their score
  @Serialize(zip=true) HashMap<ArrayList<Double>, Double> scoreMap;
  // results map: store assignments
  @Serialize(zip=true) HashMap<ArrayList<Double>, int[]> resultsMap;


  /** Learning experiments **/
  @Serialize(zip = true) HashMap<String, ArrayList<Integer>> skips;
  @Serialize(zip = true) HashMap<String, Double> rewards;
  private int currentK;
  private Key<Playlist> playlistKey;

  int curr = 0;

  public Experiment() {}

  public Experiment(String id) {
    this.id = id;
    playlistMap = new LinkedHashMap<>();
    scoreMap = new HashMap<>();
    resultsMap = new HashMap<>();
    skips = new HashMap<>();
    rewards = new HashMap<>();

    Playlist p = new Playlist();
    p.setName("Experiment-" + id);
    ofy().save().entities(p).now();
    playlistKey = Key.create(Playlist.class, p.getId());
  }

  public Playlist playlist() {
    return ofy().load().key(playlistKey).now();
  }

  public void init() {
    if(skips == null) skips = new HashMap<>();
    if(rewards == null) rewards = new HashMap<>();
  }

  /** Cluster experiment **/
  public void runNewExperiment(ArrayList<Double> featureWeights) throws Exception {
    List<SongFeature> features = new ArrayList<>(ofy().load().keys(featureKeys(playlistMap.keySet())).values());
    KMeans kMeans = new KMeans(K, features, featureWeights);
    int[] assignments = kMeans.run();
    Instances centroids = kMeans.getCentroids();
    Logger.getLogger("s").warning(centroids.toString());

    Logger.getLogger("s").warning(arrToString(assignments));
    resultsMap.put(featureWeights, assignments);
    scoreMap.put(featureWeights, evaluate(features, assignments));
  }

  /** Cluster the associated playlist.
   *  (should be called before each learning experiment) **/
  public void cluster(int K) {
    currentK = K;
    PlaylistClusterer.cluster(playlistKey.getId(), K);
    ofy().save().entities(this);
  }

  /** Learning experiment
   * Change the RewardFunction and Recommender in the resource. **/
  public void runNewRewardExperiment(String label, HypotheticalUser user, RewardFunction f, Recommender r, QLearner q) {
    String l = user.user().getId() + "-" + currentK + "-" + label;

    // No need to perform simulation for existing results. For repeated simulations
    // use RunX suffix to the label parameter.
//    if(skips.containsKey(l)) {
//      return;
//    }

    init();
    if(f != null) user.setRewardFunction(f);
    if(r != null) user.setRecommender(r);
    if(q != null) user.setQLearner(q);

    Playlist playlist = playlist();
    playlist.clearHistory();
    ofy().save().entity(playlist);

    List<String> ids = new ArrayList<>(playlist.getAllSongIds());
    String seedId = ids.get(new Random().nextInt(ids.size()));

    Pair<Double, ArrayList<Integer>> experimentResult = user.play(playlist, seedId);

    skips.put(l, experimentResult.getSecond());
    rewards.put(l, experimentResult.getFirst());

    Logger.getLogger("").warning("SKIPS " + skips.get(l) + " REWARD " + rewards.get(l));

    ofy().save().entities(this).now();
  }

  public void addPlaylist(List<SongFeature> songs) {
    Logger.getLogger("").warning(songs.size() + " songs added.");
    curr++;
    curr = (curr % K);
    for(SongFeature sf : songs) {
      if(sf != null)
        playlistMap.put(sf.getId(), curr);
    }
  }

  public void addPlaylist(String[] songs) {
    Playlist p = playlist();
    p.addSongs(songs).now();
  }

  private double evaluate(List<SongFeature> songs, int[] assignments) {
    int[][] frequencyMap = frequency(songs, assignments);
    return score(frequencyMap) + score(transpose(frequencyMap));
  }

  private int[][] frequency(List<SongFeature> songs, int[] assignments) {
    // [cluster][playlist]
    int[][] frequencyMatrix = new int[K][K];

    for(int i = 0; i < songs.size(); i++) {
      frequencyMatrix[assignments[i]][playlistMap.get(songs.get(i).getId())]++;
    }
    return frequencyMatrix;
  }

  private double score(int[][] frequencyMatrix) {
    // evaluation is the variance of the playlist Id, of each cluster
    // a good result is one where all the songs are from the same playlist and therefore
    // have the same id, low variance.
    int wrongAssignments = 0;

    for(int clusterId = 0; clusterId < K; clusterId++) {
      int[] playlistHits = frequencyMatrix[clusterId];

      // p = argmax(count(playlistHits[playlists]))
      int currMax = Integer.MIN_VALUE;
      int p = -1;
      for(int playlistId = 0; playlistId < K; playlistId++) {
        if(currMax < frequencyMatrix[clusterId][playlistId]) {
          currMax = frequencyMatrix[clusterId][playlistId];
          p = playlistId;
        }
      }

      for(int playlistId = 0; playlistId < K; playlistId++) {
        if(playlistId != p) {
          wrongAssignments += frequencyMatrix[clusterId][playlistId];
        }
      }
    }
    return (double)wrongAssignments / (double)playlistMap.size();
  }

  private int[][] transpose(int[][] matrix) {
    // rows <--> columns
    int[][] temp = new int[matrix[0].length][matrix.length];
    for (int i = 0; i < matrix.length; i++)
      for (int j = 0; j < matrix[0].length; j++)
        temp[j][i] = matrix[i][j];
    return temp;
  }

  public LinkedHashMap<String, Integer> getPlaylistMap() {
    return playlistMap;
  }

  public HashMap<ArrayList<Double>, Double> getScoreMap() {
    return scoreMap;
  }

  public HashMap<ArrayList<Double>, int[]> getResultsMap() {
    return resultsMap;
  }

  public int getK() {
    return K;
  }

  public void setK(int k) {
    K = k;
  }

  public static String arrToString(int[] arr) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for(int i : arr) sb.append(i).append(", ");
    sb.substring(0, sb.length() - 1);
    sb.append("}");
    return sb.toString();
  }

  public HashMap<String, ArrayList<Integer>> getSkips() {
    if(skips == null) {
      skips = new HashMap<>();
    }
    return skips;
  }

  public void setSkips(HashMap<String, ArrayList<Integer>> skips) {
    this.skips = skips;
  }

  public HashMap<String, Double> getRewards() {
    if(rewards == null) {
      rewards = new HashMap<>();
    }
    return rewards;
  }

  public void setRewards(HashMap<String, Double> rewards) {
    this.rewards = rewards;
  }
}
