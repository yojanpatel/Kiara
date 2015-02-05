package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Cluster {
  @Id
  private String id;

  private int level;

  private Key<NodeCluster> parent;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
    setLevel(parent.getLevel() + 1);
  }

}
