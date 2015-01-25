package uk.co.yojan.kiara.analysis.cluster;

import uk.co.yojan.kiara.analysis.features.scaling.FeatureScaler;
import uk.co.yojan.kiara.analysis.features.scaling.ZNormaliser;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.clusterers.SimpleKMeans;
import weka.core.*;

import java.util.ArrayList;
import java.util.List;

/***************************************************************************************
 *  WEKA K-MEANS http://weka.sourceforge.net/doc.dev/weka/clusterers/SimpleKMeans.html *
 ***************************************************************************************
    -N <num> Number of clusters (default 2).

    -init Initialization method to use. 0 = random, 1 = k-means++, 2 = canopy, 3 = farthest first. (default = 0)

    -C Use canopies to reduce the number of distance calculations.

    -max-candidates <num> Maximum number of candidate canopies to retain in memory at any one time when using canopy clustering.
                          T2 distance plus, data characteristics will determine how many candidate canopies are formed before
                          periodic and final pruning are performed, which might result in excess memory consumption.
                          This setting avoids large numbers of candidate canopies consuming memory. (default = 100)

    -periodic-pruning <num> How often to prune low density canopies when using canopy clustering.
                            (default = every 10,000 training instances)

    -min-density Minimum canopy density, when using canopy clustering,
                 below which a canopy will be pruned during periodic pruning. (default = 2 instances)

    -t2 The T2 distance to use when using canopy clustering.
        Values < 0 indicate that a heuristic based on attribute std. deviation should be used to set this.
        (default = -1.0)

    -t1 The T1 distance to use when using canopy clustering.
        A value < 0 is taken as a positive multiplier for T2. (default = -1.5)

    -V Display std. deviations for centroids.

    -M Don't replace missing values with mean/mode.

    -A <classname and options> Distance function to use. (default: weka.core.EuclideanDistance)

    -I <num> Maximum number of iterations.

    -O Preserve order of instances.

    -fast Enables faster distance calculations, using cut-off values. Disables the calculation/output of squared errors/distances.

    -num-slots <num> Number of execution slots. (default 1 - i.e. no parallelism)

    -S <num> Random number seed. (default 10)

    -output-debug-info If set, clusterer is run in debug mode and may output additional info to the console

    -do-not-check-capabilities If set, clusterer capabilities are not checked before clusterer is built (use with caution).
 */
public class KMeans {

  SimpleKMeans kMeans;

  FeatureScaler featureScaler = new ZNormaliser();

  private List<SongFeature> features;
  private int k;

  Instances instances;

  /* Clustering result: instance id --> cluster id */
  int[] assignments;

  /*
   * Constructor to initialise the arguments for a single run of K-Means clustering.
   *
   * @param k  number of clusters to partition the data into.
   * @param features  dataset
   */
  public KMeans(int k, List<SongFeature> features) throws Exception {
    kMeans = new SimpleKMeans();

    this.k = k;
    this.features = features;

    // Scale features
    this.instances = constructDataSet(features);
    if(featureScaler != null) {
      this.instances = featureScaler.scale(instances);
    }

    kMeans.setNumClusters(k);
    kMeans.setPreserveInstancesOrder(true);
    kMeans.setDistanceFunction(new EuclideanDistance());

  }

  public KMeans(int k, List<SongFeature> features, ArrayList<Double> featureWeights) throws Exception {
    kMeans = new SimpleKMeans();

    this.k = k;
    this.features = features;
    // Scale features
    if(featureScaler != null) {
      this.instances = featureScaler.scale(constructDataSet(features));
    } else {
      this.instances = constructDataSet(features);

    }
    kMeans.setNumClusters(k);
    kMeans.setPreserveInstancesOrder(true);

  }


  public int[] run() throws Exception {
    kMeans.buildClusterer(instances);
    assignments = kMeans.getAssignments();
    return assignments;
  }

  public Instances getCentroids() {
    return kMeans.getClusterCentroids();
  }




  public static Instances constructDataSet(List<SongFeature> data) throws IllegalAccessException {
    FastVector attInfo = new FastVector();

    /* Get all the feature names, from the annotated variables in the encapsulating
     * SongFeature class.
     */
    List<String> featureNames = SongFeature.getFeatureNames();
    for (String name : featureNames) {
      attInfo.addElement(new Attribute(name));
    }

    Instances instances = new Instances("Features", attInfo, data.size());

    for (SongFeature sf : data) {
      instances.add(new Instance(1.0, sf.getFeatureValues()));
    }

    return instances;
  }

  public static Instances constructDataSet(List<SongFeature> data, List<Double> featureWeights) throws IllegalAccessException {
    FastVector attInfo = getAttributeNames();

    Instances instances = new Instances("Features", attInfo, data.size());

    for (SongFeature sf : data) {
      double[] featureVals = sf.getFeatureValues();
      assert featureVals.length == featureWeights.size();
      for(int i = 0; i < featureWeights.size(); i++) {
        featureVals[i] = featureVals[i] * featureWeights.get(i);
      }
      instances.add(new Instance(1.0, featureVals));
    }

    return instances;
  }

  public static FastVector getAttributeNames() {
    FastVector attInfo = new FastVector();

    /* Get all the feature names, from the annotated variables in the encapsulating
     * SongFeature class.
     */
    List<String> featureNames = SongFeature.getFeatureNames();
    for (String name : featureNames) {
      attInfo.addElement(new Attribute(name));
    }
    return attInfo;
  }
}
