package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Base leaf node in the hierarchically constructed tree. It represents the song.
 *
 * Identified with a string id formatted as: <playlist id>-<song id>
 */
@Entity
public class LeafCluster extends Cluster {

//  @Id private String id;
  private int level;
//  private Cluster parent;
  private Key<NodeCluster> parent;

  // Spotify id
  private String songId;

  public LeafCluster() { }

  public LeafCluster(String songId) {
    this.songId = songId;
  }

  public List<String> getSongIds() {
    List<String> ids = new ArrayList<>();
    ids.add(songId);
    return ids;
  }

  /*
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  */

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  /* public Cluster getParent() {
    return parent;
  }

  public void setParent(Cluster parent) {
    this.parent = parent;
  } */

  public Key<NodeCluster> getParent() {
    return parent;
  }

  public void setParent(Key<NodeCluster> parent) {
    this.parent = parent;
  }

  public String getSongId() {
    return songId;
  }

  public void setSongId(String songId) {
    this.songId = songId;
  }
}
