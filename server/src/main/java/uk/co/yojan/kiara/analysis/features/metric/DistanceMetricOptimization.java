package uk.co.yojan.kiara.analysis.features.metric;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import org.apache.commons.lang.ArrayUtils;
import uk.co.yojan.kiara.analysis.cluster.WeightedEuclideanDistance;
import weka.core.Instances;
import weka.core.Optimization;

import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.analysis.features.metric.WeightList.convert;


public class DistanceMetricOptimization {

  public static int dimensions = 19;
  List<IndexPair> similar;
  List<IndexPair> different;
  Instances normalisedFeatures;
  List<Double> currWeights;
  public static final double eps = 0.1;



  public DistanceMetricOptimization(List<IndexPair> similar, List<IndexPair> different, Instances normalisedFeatures, List<Double> currWeights) {
    this.similar = similar;
    this.different = different;
    this.normalisedFeatures = normalisedFeatures;
    this.currWeights = currWeights;
  }

  public Double similarDistanceSumSq(double[] weights) {

    List<Double> weightList = new ArrayList<>();
    for(double w : weights) {
      weightList.add(w);
    }
    Logger.getLogger("").warning("Trying " + weightList.toString());
    WeightedEuclideanDistance d = new WeightedEuclideanDistance(normalisedFeatures, convert(weightList));

    Double sumSq = 0.0;

    for(IndexPair similarPair : similar) {
      double distance = d.distance(normalisedFeatures.instance(similarPair.first()),
                                   normalisedFeatures.instance(similarPair.second()));
      sumSq += distance * distance;
    }

    return sumSq;
  }

  public Double differentDistanceSum(double[] weights) {

    List<Double> weightList = new ArrayList<>();
    for(double w : weights) weightList.add(w);
    WeightedEuclideanDistance d = new WeightedEuclideanDistance(normalisedFeatures, convert(weightList));

    Double sum = 0.0;

    for(IndexPair differentPair : different) {
      double distance = d.distance(normalisedFeatures.instance(differentPair.first()),
                                   normalisedFeatures.instance(differentPair.second()));
      sum += distance;
    }

    return sum;
  }

  public double[] optimize() throws Exception {
    Optimization optimization = new Optimization() {

      HashMap<double[], Double> functionResults = new HashMap<>();

      @Override
      protected double objectiveFunction(double[] weights) throws Exception {
        if(functionResults.containsKey(weights)) {
          return functionResults.get(weights);
        } else {
          double similarSumSq = DistanceMetricOptimization.this.similarDistanceSumSq(weights);
          double differentSum = DistanceMetricOptimization.this.differentDistanceSum(weights);
          double result = similarSumSq - Math.log(differentSum);
          functionResults.put(weights, result);
          return -result;
        }
      }

      @Override
      protected double[] evaluateGradient(double[] weights) throws Exception {
        double[] grad = new double[weights.length];
        double curr = objectiveFunction(weights);

        // calculate discrete gradient for each dimension:
        // forward finite difference
        for(int dim = 0; dim < weights.length; dim++) {
          double[] aheadVector = Arrays.copyOf(weights, weights.length);

          aheadVector[dim] += eps;
          double ahead = objectiveFunction(aheadVector);

          grad[dim] = (ahead - curr) / eps;
        }
        return grad;
      }

      @Override
      protected double[] evaluateHessian(double[] weights, int i) throws Exception {
        double[] hess = new double[weights.length];

        for(int j = 0; j < hess.length; j++) {

          // denote neighbouring function values as pp (+,+), pm (+,-), mp (-,+) ,mm (-,-)
          double[] ppV = Arrays.copyOf(weights, weights.length);
          double[] pmV = Arrays.copyOf(weights, weights.length);
          double[] mpV = Arrays.copyOf(weights, weights.length);
          double[] mmV = Arrays.copyOf(weights, weights.length);

          ppV[i] += eps; ppV[j] += eps;
          pmV[i] += eps; pmV[j] -= eps;
          mpV[i] -= eps; mpV[j] += eps;
          mmV[i] -= eps; mmV[j] -= eps;

          double pp = objectiveFunction(ppV);
          double pm = objectiveFunction(pmV);
          double mp = objectiveFunction(mpV);
          double mm = objectiveFunction(mmV);

          double der = (pp - pm - mp + mm) / (4 * eps * eps);

          hess[j] = der;
        }

        return hess;
      }

      @Override
      public String getRevision() {
        return "1.0";
      }
    };

    List<Double> weights = convert(currWeights);
    // constraints for each weight is between 0-1.
    double[][] constraints = new double[2][weights.size()];
    constraints[0] = new double[weights.size()];
    constraints[1] = new double[weights.size()];
    for(int i = 0; i < weights.size(); i++) {
      constraints[0][i] = 0.0;
      constraints[1][i] = 1.0;
    }

    optimization.setMaxIteration(50);
    double[] solution = optimization.findArgmin(toDoubleArray(currWeights), constraints);
    return solution;
  }

  public static double[] toDoubleArray(List<Double> arr) {
    List<Double> list = new ArrayList<>(arr);
    return ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));
  }

  public void optimize2() {

    // Objective Function
    ConvexMultivariateRealFunction objectiveFunction = new ConvexMultivariateRealFunction() {
      HashMap<double[], Double> functionResults = new HashMap<>();

      @Override
      public double value(double[] weights) {
        if(functionResults.containsKey(weights)) {
          return functionResults.get(weights);
        } else {
          double similarSumSq = DistanceMetricOptimization.this.similarDistanceSumSq(weights);
          double differentSum = DistanceMetricOptimization.this.differentDistanceSum(weights);
          double result = similarSumSq - Math.log(differentSum);
          functionResults.put(weights, result);
          return result;
        }
      }

      @Override
      public double[] gradient(double[] weights) {

        double[] grad = new double[weights.length];
        double curr = value(weights);

        // calculate discrete gradient for each dimension:
        // forward finite difference
        for(int dim = 0; dim < weights.length; dim++) {
          double[] aheadVector = Arrays.copyOf(weights, weights.length);

          aheadVector[dim] += eps;
          double ahead = value(aheadVector);

          grad[dim] = (ahead - curr) / eps;
        }
        return grad;
      }

      @Override
      public double[][] hessian(double[] weights) {
        double[][] hess = new double[weights.length][weights.length];

        for(int i = 0; i < hess.length; i++) {
          for(int j = 0; j < hess[0].length; j++) {

            // denote neighbouring function values as pp (+,+), pm (+,-), mp (-,+) ,mm (-,-)
            double[] ppV = Arrays.copyOf(weights, weights.length);
            double[] pmV = Arrays.copyOf(weights, weights.length);
            double[] mpV = Arrays.copyOf(weights, weights.length);
            double[] mmV = Arrays.copyOf(weights, weights.length);

            ppV[i] += eps; ppV[j] += eps;
            pmV[i] += eps; pmV[j] -= eps;
            mpV[i] -= eps; mpV[j] += eps;
            mmV[i] -= eps; mmV[j] -= eps;

            double pp = value(ppV);
            double pm = value(pmV);
            double mp = value(mpV);
            double mm = value(mmV);

            double der = (pp - pm - mp + mm) / (4 * eps * eps);

            hess[i][j] = der;
          }
        }
        return hess;
      }

      @Override
      public int getDim() {
        return dimensions;
      }
    };
  }
}
