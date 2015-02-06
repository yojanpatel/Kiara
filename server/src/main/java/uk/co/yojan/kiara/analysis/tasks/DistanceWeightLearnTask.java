//package uk.co.yojan.kiara.analysis.tasks;
//
//import com.google.appengine.api.taskqueue.DeferredTask;
//import com.googlecode.objectify.Key;
//import uk.co.yojan.kiara.analysis.features.metric.DistanceMetricOptimization;
//import uk.co.yojan.kiara.analysis.features.metric.IndexPair;
//import uk.co.yojan.kiara.analysis.features.metric.SimilarPair;
//import uk.co.yojan.kiara.analysis.features.scaling.ZNormaliser;
//import uk.co.yojan.kiara.analysis.learning.EventHistory;
//import uk.co.yojan.kiara.server.models.Playlist;
//import uk.co.yojan.kiara.server.models.SongFeature;
//import weka.core.Instances;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;
//
//import static uk.co.yojan.kiara.analysis.cluster.KMeans.constructDataSet;
//import static uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer.featureKeys;
//import static uk.co.yojan.kiara.server.OfyService.ofy;
//
//
//public class DistanceWeightLearnTask implements DeferredTask {
//
//  private Long playlistId;
//
//  public DistanceWeightLearnTask(Long playlistId) {
//    this.playlistId = playlistId;
//  }
//
//  @Override
//  public void run() {
//
//    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
//    LinkedList<String> events = p.events();
//
//    List<SongFeature> features = new ArrayList<>(ofy().load().keys(featureKeys(p.getAllSongIds())).values());
//
//    HashMap<String, Integer> index = new HashMap<>();
//    for(int i = 0; i < features.size(); i++) {
//      index.put(features.get(i).getId(), i);
//    }
//
//    ArrayList<IndexPair> sim = EventHistory.similar(events, index);
//    ArrayList<IndexPair> dif = EventHistory.different(events, index);
//
//    ArrayList<SimilarPair> constrained = p.getSimilarSongs();
//    for(SimilarPair constraint : constrained) {
//      if(index.containsKey(constraint.first()) && index.containsKey(constraint.second())) {
//        sim.add(new IndexPair(index.get(constraint.first()), index.get(constraint.second())));
//      }
//    }
//
//    try {
//      // get and normalise the data
//      Instances normalised = new ZNormaliser().scale(constructDataSet(features));
//
//      // run newton raphson optimisation to find minimising weights
//      List<Double> wlist= p.getWeights();
//      if(wlist == null || wlist.isEmpty()) {
//        wlist = new ArrayList<>();
//        for(int i = 0; i < DistanceMetricOptimization.dimensions; i++) {
//          wlist.add(1.0);
//        }
//      }
//
//
//      DistanceMetricOptimization optimizer = new DistanceMetricOptimization(sim, dif, normalised, wlist);
//      double[] solution = optimizer.optimize();
//
//      // convert the vector to list of doubles
//      ArrayList<Double> weights = new ArrayList<>();
//      for(Double d : solution) weights.add(d);
//      p.setWeights(weights);
//
//      ofy().save().entity(p).now();
//    } catch (IllegalAccessException e) {
//      e.printStackTrace();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//}
