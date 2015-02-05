package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer;

import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


public class KMeansClusterTask implements DeferredTask {

  private static final Logger log = Logger.getLogger(KMeansClusterTask.class.getName());

  private String clusterId;
  private int k;

  public KMeansClusterTask(String clusterId, int k) {
    this.clusterId = clusterId;
    this.k = k;
  }

  @Override
  public void run() {
    NodeCluster node = ofy().load().key(Key.create(NodeCluster.class, clusterId)).now();
    log.warning(node.getId());
    try {
      new PlaylistClusterer().cluster(node, k);
    } catch (Exception e) {
      e.printStackTrace();
      log.warning(e.getMessage());
    }
  }
}
