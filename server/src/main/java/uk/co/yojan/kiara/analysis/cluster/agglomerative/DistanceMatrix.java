package uk.co.yojan.kiara.analysis.cluster.agglomerative;

import java.util.*;

/**
 * DataStructure that encapsulates the distances between all the songs for a playlist.
 *
 * Maintains a PriorityQueue ordered by the distances between pairs, to allow easy retrieval
 * of the next cluster pair to agglomerate.
 */
public class DistanceMatrix {

  private List<SongCluster> clusters;
  private HashMap<Long, ClusterEdge> edges;
  private PriorityQueue<ClusterEdge> distanceQueue;


  public DistanceMatrix() {
    edges = new HashMap<>();
    distanceQueue = new PriorityQueue<>();
    clusters = new ArrayList<>();
  }

  /*
   * Remove the edge with the smallest distance (head of the queue) from the matrix.
   *
   * @return  ClusterEdge that has just been removed.
   */
  public ClusterEdge head() {
    if(!distanceQueue.isEmpty()) {
      ClusterEdge headEdge = distanceQueue.poll();
      edges.remove(hashIndex(headEdge.getLeft(), headEdge.getRight()));
      assert edges.size() == distanceQueue.size();
      return headEdge;
    }
    return null;
  }

  public ClusterEdge edge(SongCluster c1, SongCluster c2) {
    return edges.get(hashIndex(c1, c2));
  }

  /*
   * Inserts a new edge to the distance matrix.
   *
   * @param c1  SongCluster representing one of the nodes
   * @param c2  SongCluster representing the other node
   * @param distance  Double representing the distance between c1 and c2
   */
  public void insert(SongCluster c1, SongCluster c2, Double distance) {
    insert(new ClusterEdge(c1, c2, distance));
  }

  public void insert(ClusterEdge edge) {
    if(edge == null) System.out.println("NULL");
    if(!distanceQueue.contains(edge)) {
      edges.put(hashIndex(edge.getLeft(), edge.getRight()), edge);
      distanceQueue.add(edge);
      assert edges.size() == distanceQueue.size();
    }
  }

  public void remove(ClusterEdge rm) {
    edges.remove(hashIndex(rm.getLeft(), rm.getRight()));
    distanceQueue.remove(rm);
    assert edges.size() == distanceQueue.size();
  }

  public boolean isEmpty() {
    assert edges.isEmpty() == distanceQueue.isEmpty();
    return edges.isEmpty();
  }

  private static long hashIndex(SongCluster c1, SongCluster c2) {
    long index = 0L;
    if(c1 != null) index += c1.hashCode();
    if(c2 != null) index += c2.hashCode();
    return index;
  }

  public void setClusters(List<SongCluster> clusters) {
    this.clusters = clusters;
  }

  public List<SongCluster> getClusters() {
    return clusters;
  }
}
