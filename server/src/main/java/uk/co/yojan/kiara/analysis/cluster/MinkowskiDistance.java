package uk.co.yojan.kiara.analysis.cluster;

import weka.core.*;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.neighboursearch.PerformanceStats;

import java.util.Enumeration;
import java.util.Vector;

public class MinkowskiDistance extends NormalizableDistance implements Cloneable, TechnicalInformationHandler {
  private static final long serialVersionUID = -7446019339455453893L;
  protected double m_Order = 2.0D;

  public MinkowskiDistance() {
  }

  public MinkowskiDistance(Instances data) {
    super(data);
  }

  public String globalInfo() {
    return "Implementing Minkowski distance (or similarity) function.\n\nOne object defines not one distance but the data model in which the distances between objects of that data model can be computed.\n\nAttention: For efficiency reasons the use of consistency checks (like are the data models of the two instances exactly the same), is low.\n\nFor more information, see:\n\n" + this.getTechnicalInformation().toString();
  }

  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation result = new TechnicalInformation(Type.MISC);
    result.setValue(Field.AUTHOR, "Wikipedia");
    result.setValue(Field.TITLE, "Minkowski distance");
    result.setValue(Field.URL, "http://en.wikipedia.org/wiki/Minkowski_distance");
    return result;
  }

  public Enumeration listOptions() {
    Vector result = new Vector();
    result.addElement(new Option("\tThe order \'p\'. With \'1\' being the Manhattan distance and \'2\'\n\tthe Euclidean distance.\n\t(default: 2)", "P", 1, "-P <order>"));
    Enumeration en = super.listOptions();

    while(en.hasMoreElements()) {
      result.addElement((Option)en.nextElement());
    }

    return result.elements();
  }

  public String orderTipText() {
    return "The order of the Minkowski distance (\'1\' is Manhattan distance and \'2\' the Euclidean distance).";
  }

  public void setOrder(double value) {
    if(this.m_Order != 0.0D) {
      this.m_Order = value;
      this.invalidate();
    } else {
      System.err.println("Order cannot be zero!");
    }

  }

  public double getOrder() {
    return this.m_Order;
  }

  public double distance(Instance first, Instance second) {
    return Math.pow(this.distance(first, second, 1.0D / 0.0), 1.0D / this.m_Order);
  }

  public double distance(Instance first, Instance second, PerformanceStats stats) {
    return Math.pow(this.distance(first, second, 1.0D / 0.0, stats), 1.0D / this.m_Order);
  }

  protected double updateDistance(double currDist, double diff) {
    double result = currDist + Math.pow(Math.abs(diff), this.m_Order);
    return result;
  }

  public void postProcessDistances(double[] distances) {
    for(int i = 0; i < distances.length; ++i) {
      distances[i] = Math.pow(distances[i], 1.0D / this.m_Order);
    }

  }

  public String getRevision() {
    return RevisionUtils.extract("$Revision: 0$");
  }
}
