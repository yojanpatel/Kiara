package uk.co.yojan.kiara.analysis.features;

import uk.co.yojan.kiara.server.echonest.data.Segment;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to perform Area method of moments for the MFCC coefficients.
 *
 * Currently using the first 3 moments, across the 2D timbre x time plane.
 *
 * Let Timbre values be denoted by X
 * Let Time values (segment) be denoted by Y
 *
 * The 9-tuple that is returned consists of the following:
 *  - E(X), E(Y), E(X^2), E(XY), E(Y^2), E(X^3), E(YX^2), E(XY^2), E(Y^3)
 *
 *  roughly moment 0 is the centre of mass, moments 1 are variance and
 *  moment 2 are shape.
 *
 *
 *  Do this for adjacent windows of 10 samples, and then average them.
 */
public class AreaMomentTimbre {

  private List<Segment> segments;
  private int timbreLen;
  private ArrayList<ArrayList<Double>> areaMoments;

  private int window_len = 10;

  public AreaMomentTimbre(List<Segment> segments) {
    areaMoments = new ArrayList<>();
    timbreLen = segments.get(0).getTimbre().size();
    this.segments = segments;
  }

  public ArrayList<Double> get() {
    if(areaMoments.isEmpty()) {
      computeAreaMoments();
    }

    // initialize
    ArrayList<Double> result = new ArrayList<>();
    for(int i = 0; i < 10; i++) {
      result.add(0.0);
    }

    // sum all of the moments for the windows
    for(ArrayList<Double> windowResult : areaMoments) {
      for(int i = 0; i < windowResult.size(); i++) {
        result.set(i, result.get(i) + windowResult.get(i));
      }
    }

    // divide by size to average
    for(int i = 0; i < result.size(); i++) {
      result.set(i, result.get(i) / areaMoments.size());
    }

    return result;
  }

  public ArrayList<ArrayList<Double>> getWindowResults() {
    if(areaMoments.isEmpty()) {
      computeAreaMoments();
    }
    return areaMoments;
  }

  private void computeAreaMoments() {
    for(int startIndex = window_len - 1; startIndex < segments.size(); startIndex++) {
      areaMoments.add(areaMomentforWindow(startIndex));
    }
  }


  private ArrayList<Double> areaMomentforWindow(int startIndex) {

    double x = 0;
    double y = 0;
    double x2 = 0;
    double y2 = 0;
    double xy = 0;
    double x3 = 0;
    double x2y = 0;
    double xy2 = 0;
    double y3 = 0;

    double sum = 0;

    // calculate normalizing coefficient
    for (int i = 0; i < window_len; i++) {
      for (int j = 0; j < timbreLen; j++) {
        sum += segments.get(startIndex - i).getTimbre().get(j);
      }
    }


    for (int i = 0; i < window_len; i++) {

      Segment segment = segments.get(startIndex - i);

      for (int j = 0; j < timbreLen; j++) {

        double timbreVal = segment.getTimbre().get(j) / sum;

        x += timbreVal * (double) i;
        y += timbreVal * (double) j;
        x2 += timbreVal * (double) i * (double) i;
        xy += timbreVal * (double) i * (double) j;
        y2 += timbreVal * (double) j * (double) j;
        x3 += timbreVal * (double) i * (double) i * (double) i;
        x2y += timbreVal * (double) i * (double) i * (double) j;
        xy2 += timbreVal * (double) i * (double) j * (double) j;
        y3 += timbreVal * (double) j * (double) j * (double) j;
      }
    }

    ArrayList<Double> areaMoments = new ArrayList<>();

    areaMoments.add(x);
    areaMoments.add(y);

    areaMoments.add(x2 - x * x);
    areaMoments.add(xy - x * y);
    areaMoments.add(y2 - y * y);

    areaMoments.add(2.0 * Math.pow(x, 3) - 3.0 * x * x2 + x3);
    areaMoments.add(2.0 * x * xy);
    areaMoments.add(2.0 * y * xy);
    areaMoments.add(2.0 * Math.pow(y, 3) - 3.0 * y * y2 + y3);

    return areaMoments;
  }
}
