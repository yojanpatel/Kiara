package uk.co.yojan.kiara.analysis.cluster;

import uk.co.yojan.kiara.analysis.cluster.linkage.ClusterLinkage;

import java.util.ArrayList;
import java.util.List;


/**
 * Agglomerative Hierarchical Clustering.
 */
public class HierarchicalClustering {

  // The clusters that are still to be agglomerated til a single one remains.
  private List<SongCluster> remainingClusters;
  private DistanceMatrix distances;
  private ClusterLinkage linkage;

  public HierarchicalClustering(DistanceMatrix distances, ClusterLinkage linkage) {
    this.distances = distances;
    this.linkage = linkage;
    remainingClusters = distances.getClusters();
  }

  private void agglomerate() {
    if(distances.isEmpty()) return;

    ClusterEdge min = distances.head();

    /*
     * Remove l = min.getLeft() and r = min.getRight() from the remaining clusters.
     * for each other cluster c:
     *   remove links (c, l) and (c, r)
     *   add link(c, n) where n is the agglomeration(l, r) to distances.
     *   add n to remaining clusters.
     */

    SongCluster leftCluster = min.getLeft();
    SongCluster rightCluster = min.getRight();
    remainingClusters.remove(leftCluster);
    remainingClusters.remove(rightCluster);

    SongCluster agglomeratedCluster = min.agglomerate();

    for(SongCluster c : remainingClusters) {
      ClusterEdge leftToC = distances.edge(leftCluster, c);
      ClusterEdge rightToC = distances.edge(rightCluster, c);

      ArrayList<Double> distanceVals = new ArrayList<>();

      if(leftToC != null) {
        distanceVals.add(leftToC.getDistance());
        distances.remove(leftToC);
      }

      if(rightToC != null) {
        distanceVals.add(rightToC.getDistance());
        distances.remove(rightToC);
      }

      Double distance = linkage.compute(distanceVals);

      ClusterEdge agglomeratedToC = new ClusterEdge(c, agglomeratedCluster, distance);
      distances.insert(agglomeratedToC);
    }
    remainingClusters.add(agglomeratedCluster);
  }

  public SongCluster run() {
    while(!clusteringComplete()) {
      agglomerate();
    }
    return remainingClusters.get(0);
  }

  /* Clustering is complete when a single root cluster node remains. */
  public boolean clusteringComplete() {
    return remainingClusters.size() == 1;
  }

  public void setRemainingClusters(ArrayList<SongCluster> remainingClusters) {
    this.remainingClusters = remainingClusters;
  }
}
