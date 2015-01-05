package uk.co.yojan.kiara.analysis.features.scaling;

import weka.core.Instances;

/**
 * Created by yojan on 1/5/15.
 */
public interface FeatureScaler {

  public Instances scale(Instances unscaled);

}
