package uk.co.yojan.kiara.analysis.cluster;

import uk.co.yojan.kiara.analysis.OfyUtils;

import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * A collection of static methods useful for various clustering and hierarchy managing algorithms.
 */
public class ClusterUtils {

  public static String clusterId(Long playlistId, int level, int index) {
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
  public static String clusterId(NodeCluster parent, int index, int k) {
    String[] s = parent.getId().split("-");
    return s[0] + "-" + (Integer.parseInt(s[1]) + 1) + "-" + (index + Integer.parseInt(s[2]) * k);
  }

  public static void deleteClusterHierarchy(Long playlistId) {
    Stack<NodeCluster> s = new Stack<>();
    Logger.getLogger("").warning("Deleting hierarchy");
    long l1 = System.currentTimeMillis();
    int counter = 0;
    NodeCluster root = OfyUtils.loadRootCluster(playlistId).now();
    if(root == null) return;
    s.push(root);
    while(!s.isEmpty()) {
      NodeCluster n = s.pop();
      if(n == null) continue;
      List<Cluster> children = n.getChildren();
      for(Cluster c : children) {
        if(c instanceof LeafCluster) {
          ofy().delete().entity(c);
          counter++;
        } else {
          s.push((NodeCluster) c);
        }
      }
      ofy().delete().entity(n);
      counter++;
    }

    long l2 = System.currentTimeMillis();
    Logger.getLogger("").warning("deleting " + counter + " took " + (l2 - l1) + "ms.");
  }

  public static void setMeanStdDev(NodeCluster cluster, List<Double> means, List<Double> stddevs) {

    Double[] meanArr = new Double[means.size()];
    Double[] stddevArr = new Double[stddevs.size()];
    means.toArray(meanArr); stddevs.toArray(stddevArr);
    cluster.setMeanStdDev(meanArr, stddevArr);
  }


}
