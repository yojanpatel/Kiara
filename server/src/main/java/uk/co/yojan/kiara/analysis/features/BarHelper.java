package uk.co.yojan.kiara.analysis.features;

import uk.co.yojan.kiara.server.echonest.data.Bar;

import java.util.ArrayList;


public class BarHelper {
  private ArrayList<Bar> bars;

  private ArrayList<Double> duration;

  private boolean sliced = false;

  public BarHelper(ArrayList<Bar> bars) {
    this.bars = bars;
    this.duration = new ArrayList<>();
  }

  /*
   * Called lazily when duration requested.
   */
  private void slice() {
    for(Bar bar : bars) {
      duration.add(bar.getDuration());
    }
    sliced = true;
  }

  public ArrayList<Double> getDuration() {
    if(!sliced) slice();
    return duration;
  }
}
