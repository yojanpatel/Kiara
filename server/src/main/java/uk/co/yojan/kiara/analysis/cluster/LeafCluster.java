package uk.co.yojan.kiara.analysis.cluster;

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

  // Spotify id
  private String songId;

  public LeafCluster() {}

  public LeafCluster(String songId) {
    this.songId = songId;
  }


  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }


  public String getSongId() {
    return songId;
  }

  public void setSongId(String songId) {
    this.songId = songId;
  }

  public double[] getNormalizedPoint() throws IllegalAccessException {
    NodeCluster parent = OfyUtils.loadNodeCluster(getParent().getName()).now();
    Double[] means = parent.getMean();
    Double[] stddevs = parent.getStddev();

    assert  means.length == stddevs.length;
    return getNormalizedPoint(means, stddevs);
  }

  public double[] getNormalizedPoint(Double[] means, Double[] stddevs) throws IllegalAccessException {
    double[] point = OfyUtils.loadFeature(songId).now().getFeatureValues();
    double[] normalized = new double[means.length];

    for(int i = 0; i < means.length; i++) {
      normalized[i] = (point[i] - means[i]) / stddevs[i];
    }

    return normalized;
  }
}
