package uk.co.yojan.kiara.analysis.features;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;

public class StatisticsTest extends TestCase {

  ArrayList<Double> singleton;
  ArrayList<Double> large;
  ArrayList<Double> onlyNegative;
  ArrayList<Double> duplicates;


  Statistics singletonStats;
  Statistics largeStats;
  Statistics negativeStats;
  Statistics duplicateStats;

  public void setUp() throws Exception {
    super.setUp();

    singleton = new ArrayList<>();
    large = new ArrayList<>();
    onlyNegative = new ArrayList<>();
    duplicates = new ArrayList<>();

    singleton.add(0.0);

    for(int i = 0; i < 100; i++) {
      large.add((double) i + (i*i)/1000.0);
      onlyNegative.add(large.get(i) * -1);
    }

    for(int i = 0; i < 10; i++) {
      duplicates.add(0.0);
    }

    singletonStats = new Statistics(singleton);
    largeStats = new Statistics(large);
    negativeStats = new Statistics(onlyNegative);
    duplicateStats = new Statistics(duplicates);
  }

  public void tearDown() throws Exception {

  }

  public void testMean() throws Exception {
    double sum = 0;
    for(Double n : large) sum += n;
    double largeMean = sum/large.size();

    assertEquals(singletonStats.mean(), 0.0);
    assertEquals(largeStats.mean(), largeMean);
    assertEquals(negativeStats.mean(), -largeMean);
    assertEquals(duplicateStats.mean(), 0.0);
  }

  public void testVariance() throws Exception {
    assertEquals(singletonStats.variance(), 0.0);
    assertEquals(duplicateStats.variance(), 0.0);

    // Assuming testMean() passes
    double largeMean = largeStats.mean();
    double sumSq = 0.0;
    for(Double d : large) sumSq += (d - largeMean) * (d - largeMean);
    double var = sumSq / (large.size() - 1);
    assertEquals(largeStats.variance(), var);
    assertEquals(negativeStats.variance(), var);
  }

  public void testSkewness() throws Exception {
    double mean = largeStats.mean();
    double sumSq = 0.0; // ^2
    double sumCb = 0.0; // ^3
    for(Double d : large) {
      sumSq += (d - mean) * (d - mean);
      sumCb += (d - mean) * (d - mean) * (d - mean);
    }
    double skewness = (sumCb / (large.size() + 1)) / Math.pow(sumSq/large.size(), 1.5);
    assertEquals(largeStats.skewness(), skewness);


    mean = negativeStats.mean();
    sumSq = 0.0; // ^2
    sumCb = 0.0; // ^3
    for(Double d : onlyNegative) {
      sumSq += (d - mean) * (d - mean);
      sumCb += (d - mean) * (d - mean) * (d - mean);
    }
    skewness = (sumCb / (onlyNegative.size() + 1)) / Math.pow(sumSq/onlyNegative.size(), 1.5);

    assertEquals(negativeStats.skewness(),skewness);


    assertEquals(singletonStats.skewness(), 0.0);

    assertEquals(duplicateStats.skewness(), Double.NaN);

  }

  public void testKurtosis() throws Exception {

  }

  public void testMedian() throws Exception {
    assertEquals(singletonStats.median(), 0.0);
    assertEquals(duplicateStats.median(), 0.0);


    assertEquals(largeStats.median(), large.get(large.size()/2));

    // ... -9, -8, -7, -6, -5, -4, -3, -2, -1, 0 (inverted)
    assertEquals(negativeStats.median(), onlyNegative.get(onlyNegative.size()/2 - (onlyNegative.size() % 2 == 0 ? 1 : 0)));
  }

  public void testRange() throws Exception {
    assertEquals(singletonStats.range(), 0.0);
    assertEquals(duplicateStats.range(), 0.0);

    assertEquals(largeStats.range(), largeStats.max() - largeStats.min());
    assertEquals(negativeStats.range(), negativeStats.max() - negativeStats.min());

    assertEquals(negativeStats.range(), largeStats.max() - largeStats.min());
  }

  public void testMin() throws Exception {
    assertEquals(singletonStats.min(), 0.0);
    assertEquals(duplicateStats.min(), 0.0);

    assertEquals(largeStats.min(), Collections.min(large));
    assertEquals(negativeStats.min(), Collections.min(onlyNegative));
  }

  public void testMax() throws Exception {
    assertEquals(singletonStats.max(), 0.0);
    assertEquals(duplicateStats.max(), 0.0);



    assertEquals(largeStats.max(), Collections.max(large));
    assertEquals(negativeStats.max(), Collections.max(onlyNegative));
  }
}