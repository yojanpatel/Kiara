package uk.co.yojan.kiara.analysis.cluster;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.features.scaling.ZNormaliser;
import uk.co.yojan.kiara.analysis.tasks.KMeansClusterTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.analysis.cluster.ClusterUtils.*;
import static uk.co.yojan.kiara.analysis.cluster.KMeans.constructDataSet;
import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * PlaylistClusterer clusters a playlist given an id, and stores the results in the DataStore.
 *
 * * * * * * * * * *
 *   CONTRACT
 * * * * * * * * * *
 *   At the moment, there is a contract that must be followed regarding the feature scaling, addition of new tracks
 *   and centroid values for the different situations.
 *
 *   1. The creator of a NodeCluster must set its centroidValues since these are computed
 *      relative to the creator. I.e. the parent must set these values at the time of adding as a child.
 *      If a NodeCluster is created as a result of a hierarchical round of clustering, these can be gathered
 *      from the KMeans class on which the .run() method was invoked.
 *
 *   2. NodeClusters must set their own mean and stddev and set them when clustering their
 *      songs hierarchically. This is because this is an independent computation that results as
 *      a side effect of the clustering by calling .getMean() and .getStdDev() on the KMeans object.
 *
 *   3. When comparing for the closest child to a certain point in the feature space:
 *      - use the centroid value point for a NodeCluster as this is a representative point for its members.
 *      - use a normalized point for a LeafCluster by calling .getNormalizedPoint() on the LeafCluster.
 *
 */
public class PlaylistClusterer {

  private static final Logger log = Logger.getLogger(PlaylistClusterer.class.getName());


  public static NodeCluster cluster(Long playlistId, int k) {

    // Fetch the playlist from persistent DataStore
    Result<Playlist> r = ofy().load().key(Key.create(Playlist.class, playlistId));

    deleteClusterHierarchy(playlistId);

    final Playlist p = r.now();
    p.setClusterReady(false);
    Collection<String> songIds = p.getAllSongIds();

    // Initialise the root node for the hierarchy
    NodeCluster root = new NodeCluster();
    root.setK(k);
    root.setId(clusterId(playlistId, 0, 0));
    root.setLevel(0);
    root.setSongIds(new ArrayList<>(songIds));

    ofy().save().entity(root).now();

    queueClusterTask(root.getId(), k);
    queueDelayedUpdate(p.getId());

    return root;
  }


  /**
   * Performs K-Means clustering on the shadow (all songs it encompasses).
   * Spawns new tasks for recursive clustering.
   *
   * @param cluster the NodeCluster to be clustered
   * @param k number of clusters to use during K-Means
   * @throws Exception
   */
  public static void cluster(NodeCluster cluster, int k) throws Exception {
    log.warning("Clustering " + cluster.getId() + " with  " + k + " clusters.");

    // Fetch the relevant SongFeature entities
    List<SongFeature> features = new ArrayList<>(ofy().load().keys(featureKeys(cluster.getSongIds())).values());

    // BASE CASE (less children than K)
    // construct the appropriate LeafClusters and assign as children
    if(cluster.getSongIds().size() <= k) {
      // collect LeafClusters for batch saving
      ArrayList<LeafCluster> leafChildren = new ArrayList<>();
      for(String songId : cluster.getSongIds()) {
        LeafCluster child = new LeafCluster(songId);
        leafChildren.add(child);
        cluster.addChild(child);
      }

      // set the mean and stddev for the songs as per the contract
      Instances instances = constructDataSet(features);
      ZNormaliser normaliser = new ZNormaliser();
      normaliser.computeMeansAndStddev(instances);
      setMeanStdDev(cluster, normaliser.getMeans(), normaliser.getStdDev());

      ofy().save().entities(cluster).now();
      ofy().save().entities(leafChildren).now();
      return;
    }


    // Perform K-Means using Weka on the feature set returning a mapping
    KMeans kMeans = new KMeans(k, features);
    int[] assignments = kMeans.run();

    // keep record of the mean and std dev. of the entire dataset for this round of clustering.
    // Store this along with the root NodeCluster responsible for this clustering
    // (cf. Contract above)
    setMeanStdDev(cluster, kMeans.getMeans(), kMeans.getStdDev());


    // Initialise the child clusters, with its centroid value as per the contract
    List<Cluster> clusters = new ArrayList<>();
    for(int i = 0; i < k; i++) {
      NodeCluster nc = new NodeCluster();

      // if there are as many clusters, as the current child index, update the centroid position
      if(i < kMeans.getCentroids().numInstances()) {
        double[] clusterCentroid = kMeans.getCentroids().instance(i).toDoubleArray();
        assert clusterCentroid.length == 103;
        nc.setCentroidValues(kMeans.getCentroids().instance(i).toDoubleArray());
      }

      clusters.add(nc);
    }


    // boolean map to keep track of which clusters have been assigned to
    boolean[] assigned  = new boolean[k];

    // Add the Spotify id associated with songIndex to the assigned cluster given by assignments[songIndex]
    for(int songIndex = 0; songIndex < assignments.length; songIndex++) {
      SongFeature song = features.get(songIndex);
      NodeCluster assignedCluster = (NodeCluster) clusters.get(assignments[songIndex]);
      assignedCluster.addSongId(song.getId());
      assigned[assignments[songIndex]] = true;
    }

    // Convert all singleton NodeCluster to LeafClusters and remove empty clusters
    for(int i = 0; i < clusters.size(); i++) {
      NodeCluster nc = (NodeCluster) clusters.get(i);

      // remove if empty cluster
      if(!assigned[i]) {
        clusters.set(i, null);
      }
      // convert singleton clusters
      else {
        LeafCluster leaf = nc.convertToLeaf();
        if (leaf != null) {
          clusters.set(i, leaf);
        }
      }
    }

    // remove all nulls
    while(clusters.remove(null));

    // Assign the clusters to the children of root
    cluster.setChildren(clusters);

    // Save to the DataStore.
    ofy().save().entity(cluster).now();
    ofy().save().entities(clusters).now();

    // Spawn child tasks for all created NodeClusters
    for(Cluster c : clusters) {
      if(c instanceof NodeCluster) {
        log.warning("Adding Cluster Task for " + c.getId());
        queueClusterTask(c.getId(), k);
      }
    }
  }


  private static void queueClusterTask(String clusterId, int k) {
    TaskManager.clusterQueue().add(
        TaskOptions.Builder
            .withPayload(new KMeansClusterTask(clusterId, k))
            .taskName("Cluster-" + clusterId+ "-" + System.currentTimeMillis()));

  }


  private static void queueDelayedUpdate(final Long playlistId) {
    TaskManager.clusterQueue().add(
        TaskOptions.Builder
            .withPayload(new DeferredTask() {
              @Override
              public void run() {
                Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
                int timeToSleep = p.size()  > 100 ? ((p.size() / 100) * 15 * 1000) : 15 * 1000;
                try {
                  Thread.sleep(timeToSleep);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                p.setClusterReady(true);
                p.setChangesSinceLastCluster(0);
                Logger.getLogger("").warning("SAVING PLAYLIST CLUSTER");
                ofy().save().entities(p);
              }
            })
            .taskName("Transaction-" + playlistId + "-" + System.currentTimeMillis()));
  }


  public static Collection<Key<SongFeature>> featureKeys(Collection<String> songIds) {
    Collection<Key<SongFeature>> featureKeys = new ArrayList<>();
    for(String id : songIds) {
      featureKeys.add(Key.create(SongFeature.class, id));
    }
    return featureKeys;
  }
}
