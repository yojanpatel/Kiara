package uk.co.yojan.kiara.analysis.features.scaling;

import org.apache.commons.lang.NotImplementedException;
import uk.co.yojan.kiara.analysis.cluster.KMeans;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

/**
 * Min-Max scaling
 *
 * Scales linearly based on the range of the data set, range of scaled feature is [0,1]
 *
 * X' = (X - min)/(max - min)
 */
public class MinMaxScaler implements FeatureScaler {

  ArrayList<Double> mins = new ArrayList<>();
  ArrayList<Double> maxs = new ArrayList<>();

  @Override
  public Instances scale(Instances unscaled) {
    computeMinMax(unscaled);
    int num = unscaled.numInstances();
    int atts = unscaled.numAttributes();
    Instances scaled = new Instances(unscaled.relationName(), KMeans.getAttributeNames(), num);

    for(int i = 0; i < num; i++) {

      double[] unscaledFeatures = unscaled.instance(i).toDoubleArray();
      double[] scaledFeatures = new double[atts];

      for(int j = 0; j < atts; j++) {
        scaledFeatures[j] = (unscaledFeatures[j] - mins.get(j))/(maxs.get(j) - mins.get(j));
        assert scaledFeatures[j] <= 1 && scaledFeatures[j] >= 0;
      }

      scaled.add(new Instance(1.0, scaledFeatures));
    }
    return scaled;
  }

  @Override
  public Instances unscale(Instances unscaled) {
    throw new NotImplementedException();
  }

  @Override
  public double[][] scale(double[][] unscaled) {
    throw new NotImplementedException();
  }

  private void computeMinMax(Instances instances) {
    int atts = instances.numAttributes();
    int num = instances.numInstances();

    // initialise
    for(int j = 0; j < atts; j++) {
      mins.add(Double.POSITIVE_INFINITY);
      maxs.add(Double.NEGATIVE_INFINITY);
    }

    // for each instance
    for(int i = 0; i < num; i++) {
      Instance curr = instances.instance(i);
      // for each attribute
      for(int j = 0; j < atts; j++) {
        double val = curr.value(j);
        mins.set(j, Math.min(mins.get(j), val));
        maxs.set(j, Math.max(maxs.get(j), val));
      }
    }
  }
}
