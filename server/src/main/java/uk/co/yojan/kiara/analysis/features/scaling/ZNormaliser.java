package uk.co.yojan.kiara.analysis.features.scaling;

import uk.co.yojan.kiara.analysis.cluster.KMeans;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

/**
 * Performs Z-Normalisation
 *
 * i.e. reshapes the date given by the input instances to a normal distribution with
 * mean 0 and standard deviation 1.
 *
 * Z = (X - mean) / stddev
 */
public class ZNormaliser implements FeatureScaler {

  ArrayList<Double> means = new ArrayList<>();
  ArrayList<Double> stddev = new ArrayList<>();



  @Override
  public Instances scale(Instances unscaled) {
    computeMeansAndStddev(unscaled);

    int num = unscaled.numInstances();
    int atts = unscaled.numAttributes();
    Instances scaled = new Instances(unscaled.relationName(), KMeans.getAttributeNames(), num);

    for(int i = 0; i < num; i++) {

      double[] unscaledFeatures = unscaled.instance(i).toDoubleArray();
      double[] scaledFeatures = new double[atts];

      for(int j = 0; j < atts; j++) {
        scaledFeatures[j] = (unscaledFeatures[j] - means.get(j)) / stddev.get(j);
      }

      scaled.add(new Instance(1.0, scaledFeatures));
    }
    return scaled;
  }

  @Override
  public Instances unscale(Instances instances) {
    assert !means.isEmpty();
    assert !stddev.isEmpty();
    int num = instances.numInstances();
    int atts = instances.numAttributes();

    Instances unscaled = new Instances(instances.relationName(), KMeans.getAttributeNames(), num);

    for(int i = 0; i < num; i++) {

      double[] scaledFeatures = instances.instance(i).toDoubleArray();
      double[] unscaledFeatures = new double[atts];

      for(int j = 0; j < atts; j++) {
        unscaledFeatures[j] = scaledFeatures[j] * stddev.get(j) + means.get(j);
      }

      unscaled.add(new Instance(1.0, unscaledFeatures));
    }
    return unscaled;
  }

  @Override
  public double[][] scale(double[][] unscaled) {

    computeMeansAndStddev(unscaled);

    int atts = unscaled[0].length;
    int num = unscaled.length;
    double[][] scaled =  new double[unscaled.length][unscaled[0].length];

    for(int i = 0; i < num; i++) {

      double[] unscaledFeatures = unscaled[i];
      double[] scaledFeatures = new double[atts];

      for(int j = 0; j < atts; j++) {
        scaledFeatures[j] = (unscaledFeatures[j] - means.get(j)) / stddev.get(j);
      }

      scaled[i] = scaledFeatures;
    }
    return scaled;
  }


  public void computeMeansAndStddev(Instances instances) {
    int atts = instances.numAttributes();
    int num = instances.numInstances();

    // initially store the sums
    for(int j = 0; j < atts; j++) {
      means.add(0.0);
      stddev.add(0.0);
    }

    // for each instance
    for(int i = 0; i < num; i++) {
      Instance curr = instances.instance(i);

      // for each attribute
      for(int j = 0; j < atts; j++) {
        double val = curr.value(j);

        // store the sum of each value
        means.set(j, means.get(j) + val);

        // store the sum of the square of each value
        stddev.set(j, means.get(j) + Math.pow(val, 2));
      }
    }

    for(int j = 0; j < atts; j++) {
      // divide by N to get mean
      means.set(j, means.get(j) / num);

      // take away the mean squared, divide by n and take square-root
      stddev.set(j, Math.sqrt(stddev.get(j) - Math.pow(means.get(j), 2)));
    }
  }

  private void computeMeansAndStddev(double[][] instances) {
    int atts = instances[0].length;
    int num = instances.length;

    // initially store the sums
    for(int j = 0; j < atts; j++) {
      means.add(0.0);
      stddev.add(0.0);
    }

    // for each instance
    for(int i = 0; i < num; i++) {
      double[] curr = instances[i];

      // for each attribute
      for(int j = 0; j < atts; j++) {
        double val = curr[j];

        // store the sum of each value
        means.set(j, means.get(j) + val);

        // store the sum of the square of each value
        stddev.set(j, means.get(j) + Math.pow(val, 2));
      }
    }

    for(int j = 0; j < atts; j++) {
      // divide by N to get mean
      means.set(j, means.get(j) / num);

      // take away the mean squared, divide by n and take square-root
      stddev.set(j, Math.sqrt(stddev.get(j) - Math.pow(means.get(j), 2)));
    }
  }

  public ArrayList<Double> getMeans() {
    return means;
  }

  public ArrayList<Double> getStdDev() {
    return stddev;
  }
}
