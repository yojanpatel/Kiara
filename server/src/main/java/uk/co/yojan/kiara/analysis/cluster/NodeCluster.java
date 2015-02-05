package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Subclass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * NodeCluster represents a single internal node in the hierarchical cluster tree.
 */
@Entity
@Subclass
public class NodeCluster extends Cluster {

//  @Id private String id;
  private int level;

  private int K;

  private ArrayList<String> children;
  private HashSet<String> leaves; // contains ids of children that are leaves

  // normalized based on the last clustering
  private double[] centroidValues;
  // to allow normalizing for possible new songs to be added
  private Double[] mean;
  private Double[] stddev;

  // shadow - all songs this cluster encompasses in a dendrogram.
  List<String> songIds;

  /* Q-learning matrix
   * Each row contains the Q-values for a given state (one of the child clusters).
   * The value Q(s, a) represents the expected sum of rewards the agent expects to receive by
   * executing the action a from. state s.*/
  @Serialize List<List<Double>> Q;

  public NodeCluster() {
    leaves = new HashSet<>();
    children = new ArrayList<>();
    songIds = new ArrayList<>();
    Q = new ArrayList<>();
  }

  public List<String> getSongIds() {
    return songIds;
  }

  public void addSongId(String songId) {
    if(!songIds.contains(songId)) {
      songIds.add(songId);
    }
  }

  public void removeSongId(String songId) {
    songIds.remove(songId);
  }

  public LeafCluster convertToLeaf() {
    if(songIds.size() == 1) {
      LeafCluster lc = new LeafCluster(songIds.get(0));
      return lc;
    } else {
      return null;
    }
  }

  public void setSongIds(List<String> songIds) {
    this.songIds = songIds;
  }

  public List<List<Double>> getQ() {
    if(Q == null || Q.isEmpty()) {
      initialiseIdentity();
    }
    return Q;
  }

  public void setQ(List<List<Double>> q) {
    Q = q;
  }

  public List<Double> getQRow(int row) {
    return Q.get(row);
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }


  /* Must ensure consistency in ordering with the other methods */
  public ArrayList<Cluster> getChildren() {

    ArrayList<Key<NodeCluster>> nodeKeys = new ArrayList<>();
    ArrayList<Key<LeafCluster>> leafKeys = new ArrayList<>();

    for(String childId : children) {
      if(childId == null) continue;
      if(leaves.contains(childId)) {
        leafKeys.add(Key.create(LeafCluster.class, childId));
      } else {
        nodeKeys.add(Key.create(NodeCluster.class, childId));
      }
    }

    ArrayList<Cluster> childNodes = new ArrayList<>();
    Map<Key<NodeCluster>, NodeCluster> clusterNodes = ofy().load().keys(nodeKeys);
    Map<Key<LeafCluster>, LeafCluster> leafNodes = ofy().load().keys(leafKeys);

    for(String id : children) {
      if(id == null) continue;
      if(leaves.contains(id)) {
        childNodes.add(leafNodes.get(Key.create(LeafCluster.class, id)));
      } else {
        childNodes.add(clusterNodes.get(Key.create(NodeCluster.class, id)));
      }
    }
    return childNodes;
  }

  public void setChildren(List<Cluster> clusters) {
    for(Cluster c : clusters) {
      if(c instanceof NodeCluster)
        addChild((NodeCluster) c);
      else
        addChild((LeafCluster) c);
    }
  }

  public void addChild(LeafCluster c) {
    // update state for the child to be added, c
    c.setLevel(getLevel() + 1);
//    c.setParent(Key.create(NodeCluster.class, getId()));
    c.setParent(this);
    c.setId(playlistId() + "-" + c.getSongId());

    leaves.add(c.getId());
    children.add(c.getId());

    // Add a 0.0 entry for all other existing children to this new child
    for(List<Double> stateRow : Q) {
      stateRow.add(0.0);
    }

    // update Q
    ArrayList<Double> stateRow = new ArrayList<>();
    for(int j = 0; j < children.size(); j++)
      stateRow.add(0.0);
    stateRow.set(children.size() - 1, 1.0);
    Q.add(stateRow);
  }

  public void addChild(NodeCluster c) {
    // update state for the child to be added, c
    c.setLevel(getLevel() + 1);
    c.setParent(Key.create(NodeCluster.class, getId()));
    c.setId(clusterId(this, children.size(), getK()));
    c.setK(getK());

    children.add(c.getId());

    // Add a 0.0 entry for all other existing children to this new child
    for(List<Double> stateRow : Q) {
      stateRow.add(0.0);
    }

    // update Q
    ArrayList<Double> stateRow = new ArrayList<>();
    for(int j = 0; j < children.size(); j++)
      stateRow.add(0.0);
    stateRow.set(children.size() - 1, 1.0);
    Q.add(stateRow);
  }

  // return the cluster index in the children list that contains songId in the shadow.
  public int songClusterIndex(String songId) {
    for(int clusterIndex = 0; clusterIndex < getChildren().size(); clusterIndex++) {
      Cluster cluster = getChildren().get(clusterIndex);

      if (cluster instanceof LeafCluster) {
        if(((LeafCluster) cluster).getSongId().equals(songId)) {
          return clusterIndex;
        }
      } else if(cluster instanceof NodeCluster) {
        if(((NodeCluster) cluster).getSongIds().contains(songId)) {
          return clusterIndex;
        }
      }
    }

    return -1;
  }

  public int nodeClusterIndex(String clusterId) {
    return getChildIds().indexOf(clusterId);
  }

  public void initialiseIdentity() {
    Q = new ArrayList<>();
    for(int i = 0; i < children.size(); i++) {
      ArrayList<Double> stateRow = new ArrayList<>();
      for(int j = 0; j < children.size(); j++) {
        stateRow.add(0.0);
      }
      stateRow.set(i, 1.0);
      Q.add(stateRow);
    }
  }


  // Essentially, addChild(replacement) but takes existing's place rather
  // than at the end as an additional child
  public int replaceChild(Cluster existing, Cluster replacement) {
    int index = children.indexOf(existing.getId());
    children.set(index, replacement.getId());

    replacement.setLevel(getLevel() + 1);
    replacement.setParent(this);

    if(replacement instanceof LeafCluster) {
      replacement.setId(playlistId() + "-" + ((LeafCluster) replacement).getSongId());
    } else if(replacement instanceof NodeCluster) {
      ((NodeCluster) replacement).setK(getK());
      replacement.setId(clusterId(this, index, getK()));
    }

    if(existing instanceof LeafCluster) {
      leaves.remove(existing.getId());
    }

    if(replacement instanceof LeafCluster) {
      leaves.add(replacement.getId());
    }

    // update Q
    ArrayList<Double> stateRow = new ArrayList<>();
    for(int j = 0; j < children.size(); j++) stateRow.add(0.0);
    stateRow.set(index, 1.0);
    Q.set(index, stateRow);

    ofy().save().entity(this).now();
    return index;
  }

  public Long playlistId() {
    return Long.parseLong(getId().split("-")[0]);
  }

  public int childIndex(Cluster c) {
    return children.indexOf(c.getId());
  }

  public double[] getCentroidValues() {
    assert centroidValues.length == 103;
    return centroidValues;
  }

  public void setCentroidValues(double[] centroidValues) {
    assert centroidValues.length == 103;
    this.centroidValues = centroidValues;

  }

  public void setMeanStdDev(Double[] mean, Double[] stddev) {
    assert mean.length == stddev.length;
    this.mean = mean;
    this.stddev = stddev;
  }

  /**
   * Normalize a point on the feature space relative to the songs
   * that are already members of the cluster from the last time an
   * actual clustering run took place.
   *
   * Z = (X - mean)/stddev
   *
   * @param point point to normalise (103 dimensions)
   * @return scaled point as a double array
   */
  public double[] normalizePoint(double[] point) {
    double[] scaled = new double[point.length];
    for(int i = 0; i < point.length; i++) {
      scaled[i] = (point[i] - mean[i]) / stddev[i];
    }
    return scaled;
  }

  public int getSize() {
    return children.size();
  }

  public boolean containsLeaf(String id) {
    return leaves.contains(id);
  }

  public List<String> getChildIds() {
    return children;
  }

  public void addLeaf(String leafId) {
    leaves.add(leafId);
  }

  public int getK() {
    return K;
  }

  public void setK(int k) {
    K = k;
  }

  public Double[] getMean() {
    return mean;
  }

  public Double[] getStddev() {
    return stddev;
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
}
