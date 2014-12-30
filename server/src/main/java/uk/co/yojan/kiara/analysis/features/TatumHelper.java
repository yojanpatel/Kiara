package uk.co.yojan.kiara.analysis.features;

import uk.co.yojan.kiara.server.echonest.data.Tatum;

import java.util.ArrayList;


public class TatumHelper {
  private ArrayList<Tatum> tatums;

  private ArrayList<Double> duration;

  private boolean sliced = false;

  public TatumHelper(ArrayList<Tatum> tatums) {
    this.tatums = tatums;
    this.duration = new ArrayList<>();
  }

  /*
   * Called lazily when duration requested.
   */
  private void slice() {
    for(Tatum tatum : tatums) {
      duration.add(tatum.getDuration());
    }
    sliced = true;
  }

  public ArrayList<Double> getDuration() {
    if(!sliced) slice();
    return duration;
  }
}
