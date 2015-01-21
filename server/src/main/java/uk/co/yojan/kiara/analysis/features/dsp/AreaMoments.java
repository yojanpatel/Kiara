package uk.co.yojan.kiara.analysis.features.dsp;

import java.util.ArrayList;

/**
 * A lightweight adaptation from JAudio.
 *
 * 2D statistical moments taken on MFCC coefficients versus time axis.
 *
 * E(X) E(Y) E(X2) E(XY) E(Y2) E(X3) E(X2Y) E(XY2) E(Y3)
 */
public class AreaMoments {

  int lookback = 10;

  double x, y, x2, xy, y2, x3, x2y, xy2, y3;

  /**
   * @param mfccWindows MFCC coefficients for the previous lookback windows.
   *                    This is the 2D plane, the statistical moments are to be
   *                    carried out on.
   *
   * @return a 9 tuple representing the above values roughly..
   */
  public ArrayList<Double> get(double[][] mfccWindows){

    ArrayList<Double> moments = new ArrayList<>();

    // to normalize the values
    double sum = 0.0;
    for (int i = 0; i < mfccWindows.length; i++) {
      for (int j = 0; j < mfccWindows[i].length; j++) {
        sum += mfccWindows[i][j];
      }
    }

    for (int i = 0; i < mfccWindows.length; i++) {
      for (int j = 0; j < mfccWindows[i].length; j++) {

        // get the window normalized coefficient.
        double normalisedMfcc = mfccWindows[i][j] / sum;

        x += normalisedMfcc * i;
        y += normalisedMfcc * j;
        x2 += normalisedMfcc * i * i;
        xy += normalisedMfcc * i * j;
        y2 += normalisedMfcc * j * j;
        x3 += normalisedMfcc * i * i * i;
        x2y += normalisedMfcc * i * i * j;
        xy2 += normalisedMfcc * i * j * j;
        y3 += normalisedMfcc * j * j * j;
      }
    }

    moments.add(x);
    moments.add(y);
    moments.add(x2 - x * x);
    moments.add(xy - x * y);
    moments.add(y2 - y * y);
    moments.add(2 * Math.pow(x, 3.0) - 3 * x * x2 + x3);
    moments.add(2 * x * xy  /*- y * x2 + x2 * y*/);
    moments.add(2 * y * xy /*- x * y2 + y2 * x*/);
    moments.add(2 * Math.pow(y, 3.0) - 3 * y * y2 + y3);

    return moments;
  }
}
