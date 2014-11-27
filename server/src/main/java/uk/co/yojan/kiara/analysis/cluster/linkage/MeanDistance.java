package uk.co.yojan.kiara.analysis.cluster.linkage;

import java.util.Collection;

public class MeanDistance implements ClusterLinkage {

  @Override
  public Double compute(Collection<Double> distances) {
    Double sum = 0.0;
    for(Double d : distances) {
      sum += d;
    }

    return sum / distances.size();
  }
}
