package uk.co.yojan.kiara.analysis.cluster;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

import java.util.List;

public class WeightedEuclideanDistance extends EuclideanDistance {

  private List<Double> weights;

  public WeightedEuclideanDistance(List<Double> weights) {
    super();
    this.weights = weights;
  }

  public WeightedEuclideanDistance(Instances data, List<Double> weights) {
    super();
    this.weights = weights;
    setInstances(weight(data));
  }


  private Instances weight(Instances data) {
    Instances weighted = new Instances(data, data.numInstances());

    for(int i = 0; i < data.numInstances(); i++) {
      double[] attVals = data.instance(i).toDoubleArray();
      for(int j = 0; j < data.numAttributes(); j++) {
        attVals[j] = attVals[j] * weights.get(j);
      }
      weighted.add(new Instance(1.0, attVals));
    }
    return weighted;
  }

  private Instance weight(Instance data) {
    double[] attVals = data.toDoubleArray();
    for(int j = 0; j < data.numAttributes(); j++) {
      attVals[j] = attVals[j] * weights.get(j);
    }
    return new Instance(1.0, attVals);
  }

  @Override
  public double distance(Instance first, Instance second) {
    return super.distance(weight(first), weight(second));
  }

  @Override
  public double distance(Instance first, Instance second, PerformanceStats stats) {
    return super.distance(weight(first), weight(second), stats);
  }
}