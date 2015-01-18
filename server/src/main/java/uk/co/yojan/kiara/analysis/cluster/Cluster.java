package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
public class Cluster {
  @Id
  private String id;

  private int level;

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

}
