package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.Vector;
import javafx.util.Pair;
import uk.co.yojan.kiara.analysis.features.WeightList;
import uk.co.yojan.kiara.analysis.features.metric.DistanceMetricOptimization;
import uk.co.yojan.kiara.analysis.features.scaling.ZNormaliser;
import uk.co.yojan.kiara.analysis.learning.EventHistory;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static uk.co.yojan.kiara.analysis.cluster.KMeans.constructDataSet;
import static uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer.featureKeys;
import static uk.co.yojan.kiara.server.OfyService.ofy;


public class DistanceWeightLearnTask implements DeferredTask {

  private Long playlistId;

  public DistanceWeightLearnTask(Long playlistId) {
    this.playlistId = playlistId;
  }

  @Override
  public void run() {

    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    LinkedList<String> events = p.events();

    List<SongFeature> features = new ArrayList<>(ofy().load().keys(featureKeys(p.getAllSongIds())).values());

    HashMap<String, Integer> index = new HashMap<>();
    for(int i = 0; i < features.size(); i++) {
      index.put(features.get(i).getId(), i);
    }

    ArrayList<Pair<Integer, Integer>> sim = EventHistory.similar(events, index);
    ArrayList<Pair<Integer, Integer>> dif = EventHistory.different(events, index);

    ArrayList<Pair<String, String>> constrained = p.getSimilarSongs();
    for(Pair<String, String> constraint : constrained) {
      sim.add(new Pair<>(index.get(constraint.getKey()), index.get(constraint.getValue())));
    }

    try {
      // get and normalise the data
      Instances normalised = new ZNormaliser().scale(constructDataSet(features));

      // run newton raphson optimisation to find minimising weights
      DistanceMetricOptimization optimizer = new DistanceMetricOptimization(sim, dif, normalised);
      Vector v = optimizer.optimize();

      // convert the vector to list of doubles
      ArrayList<Double> weights = WeightList.convert(v);
      p.setWeights(weights);

      ofy().save().entity(p).now();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
