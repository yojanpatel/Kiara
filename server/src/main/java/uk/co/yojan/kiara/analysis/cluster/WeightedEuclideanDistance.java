package uk.co.yojan.kiara.analysis.cluster;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

import java.util.List;

import static uk.co.yojan.kiara.analysis.features.metric.WeightList.normalize;

public class WeightedEuclideanDistance extends EuclideanDistance {

  private List<Double> weights;

  public WeightedEuclideanDistance(List<Double> weights) {
    super();
    this.weights = weights;
    normalize(weights);
  }

  public WeightedEuclideanDistance(Instances data, List<Double> weights) {
    super(data);
    this.weights = weights;
    setInstances(weight(data));
    normalize(weights);
    setInstances(weight(getInstances()));
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
//    Instance weighted = new Instance(data);

    for(int j = 0; j < data.numAttributes(); j++) {
      if(weights.get(j) != null && !Double.isNaN(weights.get(j))) {
        data.setValue(j, data.value(j) * weights.get(j));
      }
    }
    return data;
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