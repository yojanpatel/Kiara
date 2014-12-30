package uk.co.yojan.kiara.analysis.research;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;
import uk.co.yojan.kiara.analysis.cluster.KMeans;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer.featureKeys;
import static uk.co.yojan.kiara.server.OfyService.ofy;

@Entity
public class Experiment {

  @Id private String id;

  // number of clusters
  private int K;

  // playlist map: the spotify curated playlists
  @Serialize(zip=true)
  LinkedHashMap<String, Integer> playlistMap;

  // score map: <feature weights, K> used to their score
  @Serialize(zip=true)
  HashMap<ArrayList<Double>, Double> scoreMap;

  // results map: store assignments
  @Serialize(zip=true)
  HashMap<ArrayList<Double>, int[]> resultsMap;

  int curr = 0;

  public Experiment() {
  }

  public Experiment(String id) {
    this.id = id;
    playlistMap = new LinkedHashMap<>();
    scoreMap = new HashMap<>();
    resultsMap = new HashMap<>();
  }

  public void runNewExperiment(ArrayList<Double> featureWeights) throws Exception {
    if(resultsMap.containsKey(featureWeights)) {
      Logger.getLogger("S").info("Already have results for this feature weight comb. skipping");
      return;
    }
    List<SongFeature> features = new ArrayList<>(ofy().load().keys(featureKeys(playlistMap.keySet())).values());
    KMeans kMeans = new KMeans(K, features, featureWeights);
    int[] assignments = kMeans.run();
    Instances centroids = kMeans.getCentroids();
    Logger.getLogger("s").warning(centroids.toString());

    Logger.getLogger("s").warning(arrToString(assignments));
    resultsMap.put(featureWeights, assignments);
    scoreMap.put(featureWeights, evaluate(features, assignments));
  }

  public void addPlaylist(List<SongFeature> songs) {
    curr++;
    curr = (curr % K);
    for(SongFeature sf : songs) {
      if(sf != null)
        playlistMap.put(sf.getId(), curr);
      else
        Logger.getLogger("s").warning(sf.getId() + " was null!");
    }
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
}
