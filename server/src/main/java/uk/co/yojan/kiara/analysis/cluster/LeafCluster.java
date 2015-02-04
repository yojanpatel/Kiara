package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import uk.co.yojan.kiara.analysis.OfyUtils;

/**
 * Base leaf node in the hierarchically constructed tree. It represents the song.
 *
 * Identified with a string id formatted as: <playlist id>-<song id>
 */
@Entity
public class LeafCluster extends Cluster {

  private int level;
  private Key<NodeCluster> parent;

  // Spotify id
  private String songId;

  public LeafCluster(String songId) {
    this.songId = songId;
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

  public String getSongId() {
    return songId;
  }

  public void setSongId(String songId) {
    this.songId = songId;
  }

  public Double[] getNormalizedPoint() throws IllegalAccessException {
    NodeCluster parent = OfyUtils.loadNodeCluster(this.parent.getName()).now();
    Double[] means = parent.getMean();
    Double[] stddevs = parent.getStddev();

    assert  means.length == stddevs.length;
    return getNormalizedPoint(means, stddevs);
  }

  public Double[] getNormalizedPoint(Double[] means, Double[] stddevs) throws IllegalAccessException {
    double[] point = OfyUtils.loadFeature(songId).now().getFeatureValues();
    Double[] normalized = new Double[means.length];

    for(int i = 0; i < means.length; i++) {
      normalized[i] = (point[i] - means[i]) / stddevs[i];
    }

    return normalized;
  }
}
