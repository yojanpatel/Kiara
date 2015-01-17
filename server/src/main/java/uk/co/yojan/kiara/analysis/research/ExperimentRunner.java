package uk.co.yojan.kiara.analysis.research;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.Cluster;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.analysis.learning.recommendation.TopDownRecommender;
import uk.co.yojan.kiara.analysis.learning.QLearner;
import uk.co.yojan.kiara.analysis.learning.rewards.VariedSkipReward;
import uk.co.yojan.kiara.analysis.users.HypotheticalUser;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


public class ExperimentRunner {

  /** Change implementation of this method, to test certain parameters. **/
  public static void run(HypotheticalUser u, Experiment experiment) {

    // Currently testing various alpha values
    int REPEATS = 5;
    double start = 0.1;
    double end = 0.5;
    double step = 0.1;
    Playlist p;
    try {
      p = experiment.playlist();
      deleteSongsWithoutAnalysis(p);
    } catch(Exception e) {
      e.printStackTrace();
      return;
    }

    for (int r = 0; r < REPEATS; r++) {
      for(double currVal = start; currVal <= end; currVal += step) {
        try {
          String label = "8BPM-K9-VariedSkipReward-LearnedRecommender-Epsilon0.5-Alpha" + currVal + "-Gammma0.2-Run" + r;

          // Reset learning probability weights.
          resetQ(p.getId());


          // Override parameters based on
          final double finalCurrVal = currVal;
          experiment.runNewRewardExperiment(label, u,
              new VariedSkipReward(),
              new TopDownRecommender() {
                @Override
                public double epsilon(int level, int t) {
//                  epsilon = epsilon0 * exp(- lambda * t)
                              return 0.5 * Math.exp(-0.01 * t);
//                              return super.epsilon(level, t);
//                  return 0.0;
                }
              },
              new QLearner() {
                @Override
                public double alpha(int from, int to, int node) {
                  int closestLevel = Math.min(from, to);
                  double a = Math.pow(finalCurrVal, closestLevel - node);

                  assert a <= 1.0;
                  assert a >= 0.0;

                  return a;
                }

                @Override
                public double gamma() {
                  return 0.2;
                }
              });
        } catch (Exception e) {
          Logger.getLogger("ExperimentError").warning(e.toString());
          e.printStackTrace();
        }
      }
    }
  }


  /** Call resetQ() before every experiment start, to start fresh. **/
  private static void resetQ(Long playlistId) {
    NodeCluster root = OfyUtils.loadRootCluster(playlistId).now();
    Stack<Cluster> s = new Stack<>();
    s.push(root);
    while(!s.isEmpty()) {
      Cluster cluster = s.pop();
      if(cluster instanceof LeafCluster) continue;
      NodeCluster curr = (NodeCluster) cluster;
      curr.initialiseIdentity();
      Result r = ofy().save().entity(curr);
      ArrayList<Cluster> children = curr.getChildren();
      for(Cluster c : children) {
        if(c instanceof NodeCluster) s.push(c);
      }
      r.now();
    }
  }

  /** Certain songs do not have SongFeatures.
   *  This can mess certain experiments up, we delete these before we begin. **/
  private static void deleteSongsWithoutAnalysis(Playlist p) {
    List<Key<SongFeature>> sfKeys = ofy().load().type(SongFeature.class).keys().list();
    Collection<String> songs = p.getAllSongIds();
    Collection<String> toDelete = new ArrayList<>();
    for(String id : songs) {
      if(!sfKeys.contains(Key.create(SongFeature.class, id))) {
        toDelete.add(id);
      }
    }

    for(String id : toDelete) {
      p.removeSong(id);
    }
    ofy().save().entities(p).now();
  }
}
