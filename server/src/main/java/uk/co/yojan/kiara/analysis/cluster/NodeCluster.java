package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
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

  // shadow - all songs this cluster encompasses in a dendrogram.
  List<String> songIds;

  // Q-learning matrix
  // Each row contains the Q-values for a given state (one of the child clusters).
  // The value Q(s, a) represents the expected sum of rewards the agent expects to receive by
  // executing the action a from. state s.
  List<List<Double>> Q;

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

  public LeafCluster convertToLeaf() {
    if(songIds.size() == 1) {
      LeafCluster lc = new LeafCluster(songIds.get(0));
      lc.setId(getId());
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

  public Collection<Cluster> getChildren() {

    ArrayList<Key<NodeCluster>> nodeKeys = new ArrayList<>();
    ArrayList<Key<LeafCluster>> leafKeys = new ArrayList<>();

    for(String childId : children) {
      if(leaves.contains(childId)) {
        leafKeys.add(Key.create(LeafCluster.class, childId));
      } else {
        nodeKeys.add(Key.create(NodeCluster.class, childId));
      }
    }

    Collection<Cluster> children = new ArrayList<>();
    children.addAll(ofy().load().keys(nodeKeys).values());
    children.addAll(ofy().load().keys(leafKeys).values());
    return children;
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
  }

  public void addChild(NodeCluster c ) {
    children.add(c.getId());
  }


}
