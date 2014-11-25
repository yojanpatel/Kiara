package uk.co.yojan.kiara.analysis.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;


/**
 * A collections of static methods to calculate various statistical modes for a set of numbers.
 * Create a Statistics object for each list to be analysed.
 */
public class Statistics {

  private static Logger log = Logger.getLogger(Statistics.class.getName());

  private List<Double> samples;
  private List<Double> sorted;

  private double n;
  private double nans;
  private double sum;

  private double mean = -1;
  private double variance = -1;


  /*
   * Constructor. Given a list of doubles initialises a new object to calculate
   * various statistical modes. The constructor also pre-computes various values
   * frequently used in the algorithms.
   */
  public Statistics(List<Double> samples) {
    this.samples = samples;
    this.n = samples.size();
    for(Double d : samples) {
      if(d.isNaN()) {
        nans++;
      } else {
        sum += d;
      }
    }
    sorted = new ArrayList<>(samples);
    Collections.sort(sorted);
    log.info("Stats initialised " + n + " " + sum + " " + sorted.size());
  }

  /*
   * Computes all the statistical moments and return as an ordered tuple.
   *
   * @return <mean, variance, median, min, max, range, skewness, kurtosis> as a Double ArrayList.
   */
  public ArrayList<Double> momentVector() {
    ArrayList<Double> moments = new ArrayList<>(8);

    moments.add(mean());
    moments.add(variance());
    moments.add(median());
    moments.add(min());
    moments.add(max());
    moments.add(range());
    moments.add(skewness());
    moments.add(kurtosis());

    return moments;
  }

  /*
   * Calculates the mean in the traditional way.
   *
   * Assumptions: the samples will be between 0 and 1 inclusive, so unless
   *  an extremely huge number of sample are used, wrap around should not occur.
   *
   * @return  a double such that sum(samples)/samples.size(), NaN if dataset empty.
   */
  public Double mean() {
    if(this.mean == -1) {
      if (samples.size() == 0)
        return Double.NaN;

      this.mean =  sum / (samples.size() - nans);
    }

    return this.mean;
  }


  /*
   * Calculates the sample variance of a data set.
   *
   * Var(X) = E((X - E(X))^2)
   */
  public Double variance() {
    if(variance == -1) {

      if (samples.size() == 0)
        return Double.NaN;

      double mean = mean();
      double squaredSum = 0;

      for (Double s : samples) {
        if (!s.isNaN()) {
          squaredSum += (s - mean) * (s - mean);
        }
      }
      variance = squaredSum / (samples.size() - nans - 1);
    }
    return variance;
  }

  /*
   * Skewness is a measure of symmetry. A dataset is symmetric if it looks the same
   * to the left and right of the mean point.
   */
  public Double skewness() {
    double numeratorSum = 0;
    double denomSum = 0;

    // Evaluate the sums involved in the equation.
    for(Double s : samples) {
      double diffFromMeanSqr = (s - mean) * (s - mean);
      denomSum += diffFromMeanSqr;
      numeratorSum += diffFromMeanSqr * (s - mean);
    }

    double numerator = numeratorSum / n;
    double denominator = Math.pow(Math.sqrt(denomSum / n), 3);

    return numerator / denominator;
  }

  /*
   * Kurtosis is a measure of whether the data is peaked or flat relative to a normal
   * distribution. Data sets with high kurtosis tend to have a distinct peak near the mean,
   * decline rapidly and have heavy tails. It is the fourth standardised moment about the mean.
   */
  public Double kurtosis() {
    double coefficient = (n * (n + 1)) / ((n - 1) * (n - 2) * (n - 3));
    double offset = (3 * (n - 1) * (n - 1)) / ((n - 2) * (n - 3));
    double varSquared = variance() * variance();

    double nsum = 0;
    for(Double s : samples) {
      double diffFromMean = s - mean;
      nsum += (Math.pow(diffFromMean, 4) / varSquared) - offset;
    }
    return coefficient * nsum;
  }


  public Double median() {
    return sorted.get(sorted.size() / 2);
  }

  public Double range() {
    return max() - min();
  }

  public Double min() {
    return sorted.get(0);
  }

  public Double max() {
    return sorted.get(sorted.size() - 1);
  }

}
