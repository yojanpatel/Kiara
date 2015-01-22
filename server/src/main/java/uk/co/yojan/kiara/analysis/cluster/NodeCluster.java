package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Subclass;

import java.util.*;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * NodeCluster represents a single internal node in the hierarchical cluster tree.
 */
@Entity
@Subclass
public class NodeCluster extends Cluster {

//  @Id private String id;
  private int level;


//  private Cluster parent;
//  List<Cluster> children;

  private Key<NodeCluster> parent;
  private ArrayList<String> children;
  private HashSet<String> leaves; // contains ids of children that are leaves

  private double[] centroidValues;

  // shadow - all songs this cluster encompasses in a dendrogram.
  List<String> songIds;

  // Q-learning matrix
  // Each row contains the Q-values for a given state (one of the child clusters).
  // The value Q(s, a) represents the expected sum of rewards the agent expects to receive by
  // executing the action a from. state s.
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
    songIds.add(songId);
  }

  public void removeSongId(String songId) {
    songIds.remove(songId);
  }

  public LeafCluster convertToLeaf() {
    if(songIds.size() == 1) {
      LeafCluster lc = new LeafCluster(songIds.get(0));

      // create leaf id as <playlistId>-<songId>
      String playlistId = getId().split("-")[0];
      lc.setId(playlistId + "-" + lc.getSongId());

      lc.setLevel(getLevel());
      lc.setParent(getParent());
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


  public Key<NodeCluster> getParent() {
    return parent;
  }

  public void setParent(Key<NodeCluster> parent) {
    this.parent = parent;
  }

  public void setParent(NodeCluster parent) {
    this.parent = Key.create(NodeCluster.class, parent.getId());
  }
/*
  public HashMap<String, Key> getChildrenMap() {
    return children;
  }

  public void setChildrenMap(HashMap<String, Key> children) {
    this.children = children;
  } */

  public ArrayList<Cluster> getChildren() {

    ArrayList<Key<NodeCluster>> nodeKeys = new ArrayList<>();
    ArrayList<Key<LeafCluster>> leafKeys = new ArrayList<>();

    for(String childId : children) {
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

  public void addChild(NodeCluster c ) {
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
  public int clusterIndex(String songId) {
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

  public int replaceChild(Cluster existing, Cluster replacement) {
    int index = children.indexOf(existing.getId());
    children.set(index, replacement.getId());

    // update Q
    ArrayList<Double> stateRow = new ArrayList<>();
    for(int j = 0; j < children.size(); j++) stateRow.add(0.0);
    stateRow.set(index, 1.0);
    Q.set(index, stateRow);

    ofy().save().entity(this).now();
    return index;
  }

  public int childIndex(Cluster c) {
    return children.indexOf(c.getId());
  }

  public double[] getCentroidValues() {
    return centroidValues;
  }

  public void setCentroidValues(double[] centroidValues) {
    this.centroidValues = centroidValues;
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
}
