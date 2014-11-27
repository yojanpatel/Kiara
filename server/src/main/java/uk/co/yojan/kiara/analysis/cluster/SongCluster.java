package uk.co.yojan.kiara.analysis.cluster;

import java.util.ArrayList;

/**
 * A node object for a hierarchically clustered set of songs (playlist).
 *
 * This structure is also contains the Q-matrix that is updated during reinforcement
 * learning and queried to get the cluster to choose the next song from.
 */
public class SongCluster {

  // Cluster's unique id - will be used for Datastore persistence or querying.
  private String id;

  private SongCluster parent;
  private ArrayList<SongCluster> children;

  // Q-matrix
  // private ArrayList<ArrayList<Double>> Q;


  public SongCluster(String id) {
    this.id = id;
    this.children = new ArrayList<>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SongCluster getParent() {
    return parent;
  }

  public void setParent(SongCluster parent) {
    this.parent = parent;
  }

  public ArrayList<SongCluster> getChildren() {
    return children;
  }

  public void setChildren(ArrayList<SongCluster> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    return "SongCluster: " + id + "\nParent: " + parent.toString() + "\nChildren: " + children.toString();
  }
}
