package uk.co.yojan.kiara.analysis.features.metric;

/**
 * Created by yojan on 1/25/15.
 */
public class IndexPair {

  int indexA;
  int indexB;

  public IndexPair(int indexA, int indexB) {
    this.indexA = indexA;
    this.indexB = indexB;
  }

  public int first() {
    return indexA;
  }

  public void setFirst(int indexA) {
    this.indexA = indexA;
  }

  public int second() {
    return indexB;
  }

  public void setSecond(int indexB) {
    this.indexB = indexB;
  }
}
