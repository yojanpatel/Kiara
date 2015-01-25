//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package weka.core;

import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.neighboursearch.PerformanceStats;

import java.util.List;

public class WeightedEuclideanDistance extends NormalizableDistance implements Cloneable, TechnicalInformationHandler {
  private static final long serialVersionUID = 1068606253458807903L;

  private List<Double> weights;

  public WeightedEuclideanDistance() {
  }

  public WeightedEuclideanDistance(Instances data) {
    super(data);
  }

  public List<Double> getWeights() {
    return weights;
  }

  public void setWeights(List<Double> weights) {
    this.weights = weights;
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
    return Math.sqrt(this.distance(first, second, 1.0D / 0.0));
  }

  public double distance(Instance first, Instance second, PerformanceStats stats) {
    return Math.sqrt(this.distance(first, second, 1.0D / 0.0, stats));
  }

  public double weightedDistance(Instance first, Instance second, double cutOffValue) {
    double distance = 0.0D;
    int firstNumValues = first.numValues();
    int secondNumValues = second.numValues();
    int numAttributes = this.m_Data.numAttributes();
    int classIndex = this.m_Data.classIndex();
    this.validate();
    int p1 = 0;
    int p2 = 0;

    while(p1 < firstNumValues || p2 < secondNumValues) {
      int firstI;
      if(p1 >= firstNumValues) {
        firstI = numAttributes;
      } else {
        firstI = first.index(p1);
      }

      int secondI;
      if(p2 >= secondNumValues) {
        secondI = numAttributes;
      } else {
        secondI = second.index(p2);
      }

      if(firstI == classIndex) {
        ++p1;
      } else if(firstI < numAttributes && !this.m_ActiveIndices[firstI]) {
        ++p1;
      } else if(secondI == classIndex) {
        ++p2;
      } else if(secondI < numAttributes && !this.m_ActiveIndices[secondI]) {
        ++p2;
      } else {
        double diff;
        if(firstI == secondI) {
          diff = this.difference(firstI, first.valueSparse(p1), second.valueSparse(p2));
          ++p1;
          ++p2;
        } else if(firstI > secondI) {
          diff = this.difference(secondI, 0.0D, second.valueSparse(p2));
          ++p2;
        } else {
          diff = this.difference(firstI, first.valueSparse(p1), 0.0D);
          ++p1;
        }

        distance = this.updateDistance(distance, diff);
        if(distance > cutOffValue) {
          return 1.0D / 0.0;
        }
      }
    }

    return distance;
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
      double dist = this.distance(instance, allPoints.instance(pointList[i]), 1.0D / 0.0);
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
