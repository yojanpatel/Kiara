package uk.co.yojan.kiara.analysis.cluster;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import weka.core.Instance;
import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.neighboursearch.PerformanceStats;

public class WeightedEuclideanDistance extends NormalizableDistance implements Cloneable, TechnicalInformationHandler {
  private static final long serialVersionUID = 1068606253458807903L;



  public WeightedEuclideanDistance() {
  }

  public WeightedEuclideanDistance(Instances data) {
    super(data);
  }



  public String globalInfo() {
    return "Implementing Euclidean distance (or similarity) function.\n\nOne object defines not one distance but the data model in which the distances between objects of that data model can be computed.\n\nAttention: For efficiency reasons the use of consistency checks (like are the data models of the two instances exactly the same), is low.\n\nFor more information, see:\n\n" + this.getTechnicalInformation().toString();
  }

  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation result = new TechnicalInformation(Type.MISC);
    result.setValue(Field.AUTHOR, "Wikipedia");
    result.setValue(Field.TITLE, "Euclidean distance");
    result.setValue(Field.URL, "http://en.wikipedia.org/wiki/Euclidean_distance");
    return result;
  }

  public double distance(Instance first, Instance second) {
    return Math.sqrt(this.distance(first, second, Double.MAX_VALUE));
  }

  public double distance(Instance first, Instance second, PerformanceStats stats) {
    return Math.sqrt(this.distance(first, second, Double.MAX_VALUE, stats));
  }

  protected double updateDistance(double currDist, double diff) {
    double result = currDist + diff * diff;
    return result;
  }

  public void postProcessDistances(double[] distances) {
    for(int i = 0; i < distances.length; ++i) {
      distances[i] = Math.sqrt(distances[i]);
    }

  }

  public double sqDifference(int index, double val1, double val2) {
    double val = this.difference(index, val1, val2);
    return val * val;
  }

  public double getMiddle(double[] ranges) {
    double middle = ranges[0] + ranges[2] * 0.5D;
    return middle;
  }

  public int closestPoint(Instance instance, Instances allPoints, int[] pointList) throws Exception {
    double minDist = 2.147483647E9D;
    int bestPoint = 0;

    for(int i = 0; i < pointList.length; ++i) {
      double dist = this.distance(instance, allPoints.instance(pointList[i]), Double.MAX_VALUE);
      if(dist < minDist) {
        minDist = dist;
        bestPoint = i;
      }
    }

    return pointList[bestPoint];
  }

  public boolean valueIsSmallerEqual(Instance instance, int dim, double value) {
    return instance.value(dim) <= value;
  }

  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}
