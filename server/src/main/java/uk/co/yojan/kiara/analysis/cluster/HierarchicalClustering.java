package uk.co.yojan.kiara.analysis.cluster;

import uk.co.yojan.kiara.analysis.cluster.linkage.ClusterLinkage;


/**
 * Agglomerative Hierarchical Clustering.
 */
public class HierarchicalClustering {

  private DistanceMatrix distances;
  private ClusterLinkage linkage;

  public HierarchicalClustering(DistanceMatrix distances, ClusterLinkage linkage) {
    this.distances = distances;
    this.linkage = linkage;
  }

  public void run() {
    if(distances.isEmpty()) return;

    ClusterEdge min = distances.head();
    /*
     * Remove l = min.getLeft() and r = min.getRight() from wherever.
     * for each other cluster c':
     *   remove links (c', l) and (c', r)
     *   add link(c', n) where n is the agglomeration(l, r) to distances.
     */

  }
}
