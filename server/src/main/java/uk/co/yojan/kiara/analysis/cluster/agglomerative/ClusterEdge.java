package uk.co.yojan.kiara.analysis.cluster.agglomerative;

/**
 * Encapsulates a single edge in the hierarchically clusterd dendrogram.
 *
 * Objects of this class are stored in the DistanceMatrix object for each playlist,
 * ordered by their distance.
 */
public class ClusterEdge implements Comparable<ClusterEdge> {

  /*
   * Flag used by hashCode - If true, c1-->c2 and c2-->c1 are treated as different
   * edges and therefore are both present in DistanceMatrix's queue.
   */
  private static final boolean BIDRECTIONAL = false;

  private SongCluster left;
  private SongCluster right;

  /* distance between the left and right cluster. */
  private Double distance;

  public ClusterEdge(SongCluster left, SongCluster right, Double distance) {
    this.left = left;
    this.right = right;
    this.distance = distance;
  }

  public SongCluster getLeft() {
    return left;
  }

  public void setLeft(SongCluster left) {
    this.left = left;
  }

  public SongCluster getRight() {
    return right;
  }

  public void setRight(SongCluster right) {
    this.right = right;
  }

  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  /*
   * Builder for a new ClusterEdge as a result from the agglomeration of two
   * SongClusters involved in this edge.
   *
   * @return SongCluster representing the new node created in the hierarchy.
   */
  public SongCluster agglomerate() {
    // Id of an agglomerated node is the concatenation of the two children ids separated by a dash.
    String clusterId = (left == null ? "" : left.getId()) + (right == null ? "" : ("-" + right.getId()));
    SongCluster cluster = new SongCluster(clusterId);

    cluster.getChildren().add(left);
    cluster.getChildren().add(right);
    left.setParent(cluster);
    right.setParent(cluster);

    return cluster;
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   */
  @Override
  public int compareTo(ClusterEdge o) {
    System.out.println(o == null);
    System.out.println(o.getDistance());
    return getDistance().compareTo(o.getDistance());
  }

  /**
   * Returns a hash code value for the object. This method is
   * supported for the benefit of hash tables such as HashMap.
   */
  @Override
  public int hashCode() {
    if(BIDRECTIONAL) {
      return super.hashCode();
    } else {
      return left.hashCode() + right.hashCode();
    }
  }
}
