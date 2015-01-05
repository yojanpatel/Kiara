package uk.co.yojan.kiara.analysis.cluster.agglomerative;


import uk.co.yojan.kiara.analysis.cluster.agglomerative.ClusterEdge;
import uk.co.yojan.kiara.analysis.cluster.agglomerative.DistanceMatrix;
import uk.co.yojan.kiara.analysis.cluster.agglomerative.HierarchicalClustering;
import uk.co.yojan.kiara.analysis.cluster.agglomerative.SongCluster;
import uk.co.yojan.kiara.analysis.cluster.linkage.MeanDistance;

import java.util.*;

public class Test {

  private static DistanceMatrix createLinkages(double[][] distances,
                                     List<SongCluster> clusters) {
    DistanceMatrix linkages = new DistanceMatrix();
    for (int col = 0; col < clusters.size(); col++) {
      for (int row = col + 1; row < clusters.size(); row++) {
        ClusterEdge link = new ClusterEdge(clusters.get(col),clusters.get(row), distances[col][row]);
        linkages.insert(link);
      }
    }
    return linkages;
  }

  private static List<SongCluster> createClusters(String[] clusterNames) {
    List<SongCluster> clusters = new ArrayList<SongCluster>();
    for (String clusterName : clusterNames) {
      SongCluster cluster = new SongCluster(clusterName);
      clusters.add(cluster);
    }
    return clusters;
  }

  private static double spacing(int max, int level) {
    return 2 * (Math.pow(2, max - level + 1) - 1);
  }

  private static double indent(int max, int level) {
    return 2 * (Math.pow(2, max - level) - 1);
  }

  private static int depth(SongCluster c) {
    HashMap<SongCluster, Integer> depthMap = new HashMap<>();
    Queue<SongCluster> cq = new LinkedList<>();

    cq.add(c);
    depthMap.put(c, 1);

    while(!cq.isEmpty()) {
      SongCluster head = cq.poll();
      if(head.getParent() != null) {
        depthMap.put(head, depthMap.get(head.getParent()) + 1);
      }
      System.out.println(head.getId() + (head.getParent() != null ? " parent: " + head.getParent().getId() : ""));
      cq.addAll(head.getChildren());
    }

    int max = 0;
    for(int depth : depthMap.values()) {
      max = Math.max(max, depth);
    }
    return max;
  }

  public static void main(String[] args) {
    String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
    double[][] distances = new double[][] {
        { 0, 1, 9, 7, 11, 14 },
        { 1, 0, 4, 3, 8, 10 },
        { 9, 4, 0, 9, 2, 8 },
        { 7, 3, 9, 0, 6, 13 },
        { 11, 8, 2, 6, 0, 10 },
        { 14, 10, 8, 13, 10, 0 }};

    DistanceMatrix matrix = new DistanceMatrix();
    ArrayList<SongCluster> clusters = new ArrayList<>(createClusters(names));
    HierarchicalClustering alg = new HierarchicalClustering(createLinkages(distances, clusters), new MeanDistance());
    alg.setRemainingClusters(clusters);
    SongCluster c = alg.run();

    HashMap<SongCluster, Integer> depthMap = new HashMap<>();
    int maxLevel = depth(c);

    Queue<SongCluster> cq = new LinkedList<>();
    cq.add(c);
    depthMap.put(c, 1);
    int lastLevel = 0;

    while(!cq.isEmpty()) {
      SongCluster head = cq.poll();
      int currLevel = head.getParent() == null ? 1 : depthMap.get(head.getParent()) + 1;

      // New level
      if(currLevel == lastLevel + 1) {
        System.out.print("\n");
        lastLevel = currLevel;
        for(int i = 0; i < indent(maxLevel, currLevel); i++) {
          System.out.print(" ");
        }
      }

      if(head.getParent() != null) {
        depthMap.put(head, currLevel);
      }

      if(head.getChildren().isEmpty())
        System.out.print(head.getId());
      else
        System.out.println("***");

      for(int i = 0; i < spacing(maxLevel, currLevel); i++) {
        System.out.print(" ");
      }

      cq.addAll(head.getChildren());
    }
  }
}
