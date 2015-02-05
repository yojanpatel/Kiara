package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.*;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


/**
 * Please read the contract detailed in PlaylistClusterer.java as it explains
 * vital details as to how to choose the optimal child from a root cluster into
 * which the song should be inserted.
 *
 * The key detail is values to be compared for euclidean distance minimization
 * are NodeCluster.getCentroidValues and LeafCluster.getNormalizedPoint().
 *
 * A SongFeature's normalized point relative to a NodeCluster can be computed by using
 * the cluster's mean and stddev (approximate).
 *
 * Call cluster.normalizePoint(Double[] point)
 */
public class BatchAddSongTask implements DeferredTask {

  private String[] songIds;
  private Long playlistId;

  HashMap<Key<NodeCluster>, NodeCluster> nodes;
  HashMap<Key<LeafCluster>, LeafCluster> leaves;
  Map<Key<SongFeature>, SongFeature> songFeatures;

  boolean incomplete;

  /**
   * Constructor for the task
   *
   * @param songIds  songIds to be inserted.
   * @param playlistId playlist whose cluster tree the songs are to be inserted into.
   */
  public BatchAddSongTask(String[] songIds, Long playlistId) {
    this.songIds = songIds;
    this.playlistId = playlistId;
    nodes = new HashMap<>();
    leaves = new HashMap<>();
    songFeatures = new HashMap<>();
  }


  @Override
  public void run() {
    loadFeatures(songIds);

    // each addition starts at the root and moves greedily towards
    // the closest branch until a base case is reached:
    // 1. Song can be inserted as an extra child
    // 2. Song can be agglomerated with another Leaf.
    NodeCluster root = OfyUtils.loadRootCluster(playlistId).now();


    // Add each song sequentially
    for(String songId : songIds) {

      NodeCluster currentCluster = root;


      // skip tracks that are already present in the hierarchy.
      // situation can arise if a single some tracks in a BatchAdd
      // succeeded while others didn't as the task is still retried.
      if(currentCluster.getSongIds().contains(songId)) {
        Logger.getLogger("").warning("Already contains it!");
        continue; // for loop, done with this track, add the next one.
      }

      // record if a song was not available so the task can be retried
      // later when the SongFeature may be available.
      SongFeature song = loadFeature(songId);
      if (song == null) {
        incomplete = true;
        continue; // for loop, done with this track, add the next one.
      }

      // TODO: remove while(true) as no code should ever use while(true) Ew.
      while (true) {
        List<Cluster> children = getChildren(currentCluster);

        // BASE CASE:
        // if the cluster has an available slot for a new Leaf, use it.
        if(children.size() < currentCluster.getK()) {
          LeafCluster newLeaf = new LeafCluster(songId);
          currentCluster.addChild(newLeaf);
          leaves.put(Key.create(LeafCluster.class, newLeaf.getId()), newLeaf);
          break; // while loop, done with this track, add the next one.
        }

        // ITERATIVE CASE:
        // find the child to which this song belongs and add/agglomerate to it
        Cluster closestCluster = currentCluster;
        Double closestCentroidDistance = Double.MAX_VALUE;

        // Linear search O(K), through all the children to find the closest one
        // using a Euclidean distance metric on the normalized (relative to currentCluster's shadow).
        for (Cluster child : children) {
          double d = Integer.MAX_VALUE;
          try {

            // Two cases: NodeCluster centroid OR singleton LeafCluster
            // Read Notes above and Contract in PlaylistClusterer to see how these comparisons work.
            double[] songNormalizedFeaturePoint = currentCluster.normalizePoint(song.getFeatureValues());

            if (child instanceof NodeCluster) {
              d = distance(
                  new Instance(1.0, songNormalizedFeaturePoint),
                  new Instance(1.0, ((NodeCluster) child).getCentroidValues()));

            } else if (child instanceof LeafCluster) {
              double[] leafNormalizedFeaturePoint =
                  ((LeafCluster) child).getNormalizedPoint(currentCluster.getMean(), currentCluster.getStddev());

              d = distance(
                  new Instance(1.0, leafNormalizedFeaturePoint), new Instance(1.0, songNormalizedFeaturePoint));

            }
          } catch (IllegalAccessException e) {
            Logger.getLogger("").warning(e.getMessage());
          }

          // update current best child, if found a smaller distance
          if (d < closestCentroidDistance) {
            closestCentroidDistance = d;
            closestCluster = child;
          }
        }

        currentCluster.addSongId(song.getId());

        // BASE CASE II:
        // If the closest child happens to be a Leaf, agglomerate the new Leaf
        // with the existing one.
        if (closestCluster instanceof LeafCluster) {
          // agglomerate the leaf cluster and the song to be added
          agglomerateCluster((LeafCluster) closestCluster, song);
          break; // while loop, done with this track, add the next one.

        // ITERATE: Otherwise need to go deeper into the hierarchy to find a more suitable cluster.
        } else if (closestCluster instanceof NodeCluster) {

          // update the clusters along the path's shadow, check if key exists in root's ids.
          nodes.put(Key.create(NodeCluster.class, currentCluster.getId()), currentCluster);

          // recursively add to the child cluster
          currentCluster = (NodeCluster) closestCluster;
        }
      }
    }

    saveAll();
    if(incomplete) {
      throw new NullPointerException("No analysis found for some songs. Will retry later.");
    }
  }

  // parent -> leaf
  //
  // parent -> agglomerated -> {leaf, newLeaf}

  /**
   *
   *   PARENT ---> LEAF
   *
   *                                 ---> LEAF
   *   PARENT ---> AGGLOMERATED ----|
   *                                 ---> NEW_SONG
   *
   * Don't forget the contract for the created agglomerated NodeCluster.
   *
   * @param leaf LEAF
   * @param song NEW_SONG
   */
  private void agglomerateCluster(LeafCluster leaf, SongFeature song) {

    NodeCluster parent = loadNode(leaf.getParent());
    NodeCluster agglomerated = new NodeCluster();
    LeafCluster newLeaf = new LeafCluster(song.getId());

    // replace child link for the parent to the new agglomerated node instead of the singleton
    parent.replaceChild(leaf, agglomerated);
    agglomerated.addChild(leaf);
    agglomerated.addChild(newLeaf);

    // Normalize LEAF AND NEW_LEAF points relative to PARENT,
    // average to get the centroid for AGGLOMERATED
    try {
      double[] existingLeafNormalized = leaf.getNormalizedPoint(parent.getMean(), parent.getStddev());
      double[] newLeafNormalized = newLeaf.getNormalizedPoint(parent.getMean(), parent.getStddev());
      agglomerated.setCentroidValues(average(existingLeafNormalized, newLeafNormalized));
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }


    // average the points for the two leaves for the agglomerated centroid value
    SongFeature existingLeafSong = OfyUtils.loadFeature(leaf.getSongId()).now();
    try {
      double[] newSongsVals = song.getFeatureValues();
      double[] existingSongVals = existingLeafSong.getFeatureValues();
      double[] aggCentroid = average(newSongsVals, existingSongVals);
      agglomerated.setCentroidValues(aggCentroid);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    // TODO: consider options
    // Borrow parent's mean and stddev.
    agglomerated.setMeanStdDev(parent.getMean(), parent.getStddev());

    nodes.put(Key.create(NodeCluster.class, parent.getId()), parent);
    nodes.put(Key.create(NodeCluster.class, agglomerated.getId()), agglomerated);
    leaves.put(Key.create(LeafCluster.class, newLeaf.getId()), newLeaf);
    leaves.put(Key.create(LeafCluster.class, leaf.getId()), leaf);
  }


  private double distance(Instance first, Instance second) {
    EuclideanDistance distanceFunction = new EuclideanDistance();
    Instances instances = new Instances("Distance", KMeans.getAttributeNames(), 2);
    instances.add(first);
    instances.add(second);
    distanceFunction.setInstances(instances);

    return distanceFunction.distance(instances.instance(0), instances.instance(1));
  }

  private double[] average(double[] a, double[] b) {
    assert a.length == b.length;
    double[] ave = new double[a.length];
    for(int i = 0; i < a.length; i++) {
      ave[i] = 0.5 * (a[i] + b[i]);
    }
    return ave;
  }

  private LeafCluster loadLeaf(Key<LeafCluster> key) {
    if(leaves.containsKey(key)) {
      return leaves.get(key);
    } else {
      LeafCluster leaf = ofy().load().key(key).now();
      leaves.put(key, leaf);
      return leaf;
    }
  }

  private NodeCluster loadNode(Key<NodeCluster> key) {
    if(nodes.containsKey(key)) {
      return nodes.get(key);
    } else {
      NodeCluster node = ofy().load().key(key).now();
      nodes.put(key, node);
      return node;
    }
  }

  private void loadFeatures(String[] songIds) {
    List<Key<SongFeature>> keys = new ArrayList<>();
    for(String id : songIds) keys.add(Key.create(SongFeature.class, id));
    songFeatures = ofy().load().keys(keys);
  }

  private SongFeature loadFeature(String id) {
    return songFeatures.get(Key.create(SongFeature.class, id));
  }

  List<Cluster> getChildren(NodeCluster cluster) {
    List<Cluster> children = new ArrayList<>();
    List<String> childIds = cluster.getChildIds();
    for(String id : childIds) {
      if(cluster.containsLeaf(id)) {
        children.add(loadLeaf(Key.create(LeafCluster.class, id)));
      } else {
        children.add(loadNode(Key.create(NodeCluster.class, id)));
      }
    }
    return children;
  }

  private void saveAll() {
    Collection<NodeCluster> ns = nodes.values();
    ns.removeAll(Collections.singleton(null));

    Collection<LeafCluster> ls = leaves.values();
    ls.removeAll(Collections.singleton(null));

    Result r1 = ofy().save().entities(ns);
    Result r2 = ofy().save().entities(ls);
    r1.now();
    r2.now();
  }
}
