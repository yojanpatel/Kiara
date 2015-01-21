package uk.co.yojan.kiara.analysis.features.dsp;

import uk.co.yojan.kiara.analysis.features.dsp.orangecow.MfccExtraction;


public class MFCC {

   MfccExtraction fe;

  private int sampleRate;

  /**
   * Construct a MFCC extraction object provided by orange cow.
   */
  public MFCC(int sampleRate) {
    fe = new MfccExtraction();
    this.sampleRate = sampleRate;
  }

  /**
   * Calculate Mel Frequency Cepstrum Coeffecients.
   *
   * @param magnitudeSpectrum the FFT output on a window.

   */
  public double[] extractFeature(double[] magnitudeSpectrum) throws Exception {

    int[] fftBinIndices = fe.fftBinIndices(sampleRate, magnitudeSpectrum.length);
    double[] filterBank = fe.melFilter(magnitudeSpectrum, fftBinIndices);
    double[] f = fe.nonLinearTransformation(filterBank);
    double[] coeff = fe.cepCoefficients(f);
    return coeff;
  }
}
