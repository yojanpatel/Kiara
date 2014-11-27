package uk.co.yojan.kiara.analysis.cluster.linkage;

import java.util.Collection;

/**
 * Following the Strategy design pattern in order to choose
 * which Linkage (decision as to how the inter-cluster distance
 * should be calculated) algorithm to use.
 */
public interface ClusterLinkage {
  public Double compute(Collection<Double> distances);
}
