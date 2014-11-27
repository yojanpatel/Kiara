package uk.co.yojan.kiara.analysis.cluster;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.*;

/**
 * DataStructure that encapsulates the distances between all the songs for a playlist.
 *
 * Maintains a PriorityQueue ordered by the distances between pairs, to allow easy retrieval
 * of the next cluster pair to agglomerate.
 */
public class DistanceMatrix {

  private HashSet<ClusterEdge> edges;
  private PriorityQueue<ClusterEdge> distanceQueue;


  public DistanceMatrix() {
    edges = new HashSet<>();
    distanceQueue = new PriorityQueue<>();
  }

  /*
   * Remove the edge with the smallest distance (head of the queue) from the matrix.
   *
   * @return  ClusterEdge that has just been removed.
   */
  public ClusterEdge head() {
    if(!distanceQueue.isEmpty()) {
      ClusterEdge headEdge = distanceQueue.poll();
      edges.remove(headEdge);
      assert edges.size() == distanceQueue.size();
      return headEdge;
    }
    return null;
  }

  public ClusterEdge edge(SongCluster c1, SongCluster c2) {
    throw new NotImplementedException();
  }

  public void remove(ClusterEdge rm) {
    edges.remove(rm);
    distanceQueue.remove(rm);
    assert edges.size() == distanceQueue.size();
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
    if(!edges.contains(edge)) {
      edges.add(edge);
      distanceQueue.add(edge);
      assert edges.size() == distanceQueue.size();
    }
  }

  public boolean isEmpty() {
    assert edges.isEmpty() == distanceQueue.isEmpty();
    return edges.isEmpty();
  }
}
