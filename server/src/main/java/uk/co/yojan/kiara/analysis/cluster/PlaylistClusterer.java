package uk.co.yojan.kiara.analysis.cluster;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.tasks.KMeansClusterTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * PlaylistClusterer clusters a playlist given an id, and stores the results in the DataStore.
 */
public class PlaylistClusterer {

  private static final Logger log = Logger.getLogger(PlaylistClusterer.class.getName());


  public static NodeCluster cluster(Long playlistId, int k) {

    // Fetch the playlist from persistent DataStore
    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    Collection<String> songIds = p.getAllSongIds();

    // Initialise the root node for the hierarchy
    NodeCluster root = new NodeCluster();
    root.setId(clusterId(playlistId, 0, 0));
    root.setLevel(0);
    root.setSongIds(new ArrayList<>(songIds));

    ofy().save().entity(root).now();

    queueClusterTask(root.getId(), k);

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

    // Perform K-Means using Weka on the feature set returning a mapping
    KMeans kMeans = new KMeans(k, features);
    int[] assignments = kMeans.run();

    // Initialise the child clusters
    List<Cluster> clusters = new ArrayList<>();
    for(int i = 0; i < k; i++) {
      NodeCluster nc = new NodeCluster();
      nc.setParent(cluster);
      nc.setLevel(cluster.getLevel() + 1);
      nc.setId(clusterId(cluster, i, k));
      clusters.add(nc);
    }

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

  private static Collection<Key<SongFeature>> featureKeys(List<String> songIds) {
    Collection<Key<SongFeature>> featureKeys = new ArrayList<>();
    for(String id : songIds) {
      featureKeys.add(Key.create(SongFeature.class, id));
    }
    return featureKeys;
  }

  private static String clusterId(Long playlistId, int level, int index) {
    return playlistId + "-" + level + "-" + index;
  }

  /**
   * Constructs the cluster id in the form <playlist id>-<level>-<index in level>
   *
   * @param parent the predecessor of the cluster node in the hierarchy
   * @param index  the child index for the parent, not the entire level
   * @param k  the number of clusters for K-Means for level-wide index approximation
   * @return  a String in the above format representing the id of the ClusterNode
   */
  private static String clusterId(NodeCluster parent, int index, int k) {
    String[] s = parent.getId().split("-");
    return s[0] + "-" + (Integer.parseInt(s[1]) + 1) + "-" + (index + Integer.parseInt(s[2]) * k);
  }

  public static void main(String[] args) {
    NodeCluster p = new NodeCluster();
    p.setId("23233-1-2");
    System.out.println(clusterId(p, 0, 4));
    System.out.println(clusterId(p, 1, 4));
    System.out.println(clusterId(p, 2, 4));
    System.out.println(clusterId(p, 3, 4));
  }
}
