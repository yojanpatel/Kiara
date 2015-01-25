package uk.co.yojan.kiara.analysis.features.metric;

/**
 * Created by yojan on 1/25/15.
 */
public class SimilarPair {

  String songA;
  String songB;

  public SimilarPair(String songA, String songB) {
    this.songA = songA;
    this.songB = songB;
  }

  public String first() {
    return songA;
  }

  public void setFirst(String songA) {
    this.songA = songA;
  }

  public String second() {
    return songB;
  }

  public void setSecond(String songB) {
    this.songB = songB;
  }


  @Override
  public String toString() {
    return first() + "-" + second();
  }

  public SimilarPair fromString(String s) {
    String[] parts = s.split("-");
    return new SimilarPair(parts[0], parts[1]);
  }
}
