package uk.co.yojan.kiara.analysis.features;


import uk.co.yojan.kiara.analysis.features.dsp.AreaMoments;
import uk.co.yojan.kiara.analysis.features.dsp.FFT;
import uk.co.yojan.kiara.analysis.features.dsp.MFCC;

import java.util.ArrayList;

public class MFCCAreaMoment {

  double[] samples;

  FFT fft;

  int lookback = 10;
  int windowSize = 512; // 2^9
  int sampleRate = 16 * 1000; // 16kHz

  double[][] windowMagnitudeSpectrums;
  double[][] windowMfcc;
  ArrayList<ArrayList<Double>> windowMoments;

  public MFCCAreaMoment(double[] samples) throws Exception {
    this.samples = samples;
    windowMagnitudeSpectrums = new double[(int) Math.ceil(samples.length / (double)windowSize)][];
    windowMfcc = new double[windowMagnitudeSpectrums.length][];
    windowMoments = new ArrayList<>();
  }

  public ArrayList<ArrayList<Double>> getWindowMoments(){
    if(windowMoments.isEmpty()) {
      try {
        compute();
      } catch (Exception e) {
        return null;
      }
    }
    return windowMoments;
  }

  private void compute() throws Exception {
    performWindowedFFT();
    extractMFCCs();
    adjacentWindowMoments(lookback);
  }


  private void performWindowedFFT() throws Exception {
    int windowIndex = 0;
    int windowStart = 0;
    while(windowStart < samples.length) {

      // prepare window
      double[] window = new double[windowSize];
      for(int windowOffset = 0; windowOffset < windowSize; windowOffset++) {
        int index = windowStart + windowOffset;
        if(index < samples.length) {
          window[windowOffset] = samples[windowStart + windowOffset];
        } else {
          window[windowOffset] = 0.0; // zero-pad the last window if needed
        }
      }

      // perform fft on the window
      double[] magspec = new FFT(window, null, false, true).getMagnitudeSpectrum();
      windowMagnitudeSpectrums[windowIndex] = magspec;
      windowStart += windowSize;
      windowIndex++;
    }
  }

  private void extractMFCCs() throws Exception {
    MFCC mfcc = new MFCC(sampleRate);
    for(int i = 0; i < windowMagnitudeSpectrums.length; i++) {
      windowMfcc[i] = mfcc.extractFeature(windowMagnitudeSpectrums[i]);
    }
  }

  private void adjacentWindowMoments(int lookback) {

    AreaMoments areaMoments = new AreaMoments();

    for(int t = lookback - 1; t < windowMfcc.length; t++) {

      double[][] lookbackMfccs = new double[lookback][];
      for (int i = 0; i < lookback; i++) {
        lookbackMfccs[i] =  windowMfcc[t - i];
      }
      ArrayList<Double> moments = areaMoments.get(lookbackMfccs);
      windowMoments.add(moments);
    }
  }

  public ArrayList<Double> averageMoments() throws Exception {
    if(windowMoments.isEmpty()) {
      compute();
    }

    ArrayList<Double> averages = new ArrayList<>();

    for(int i = 0; i < windowMoments.get(0).size(); i++) {
      averages.add(0.0);
    }

    // sum
    for(ArrayList<Double> windowMoment : windowMoments) {
      for(int i = 0; i < windowMoment.size(); i++) {
        averages.set(i, averages.get(i) + windowMoment.get(i));
      }
    }

    // average
    for(int i = 0; i < averages.size(); i++) {
      averages.set(i, averages.get(i) / windowMoments.size());
    }

    return averages;
  }
}
