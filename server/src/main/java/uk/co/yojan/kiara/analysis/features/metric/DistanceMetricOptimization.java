package uk.co.yojan.kiara.analysis.features.metric;

import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.Matrix;
import com.numericalmethod.suanshu.algebra.linear.matrix.doubles.matrixtype.dense.DenseMatrix;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.Vector;
import com.numericalmethod.suanshu.algebra.linear.vector.doubles.dense.DenseVector;
import com.numericalmethod.suanshu.analysis.function.matrix.RntoMatrix;
import com.numericalmethod.suanshu.analysis.function.rn2r1.RealScalarFunction;
import com.numericalmethod.suanshu.analysis.function.rn2rm.RealVectorFunction;
import com.numericalmethod.suanshu.optimization.multivariate.unconstrained.steepestdescent.NewtonRaphsonMinimizer;
import com.numericalmethod.suanshu.optimization.problem.C2OptimProblem;
import com.numericalmethod.suanshu.optimization.problem.IterativeMinimizer;
import javafx.util.Pair;
import uk.co.yojan.kiara.analysis.cluster.WeightedEuclideanDistance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DistanceMetricOptimization {

  private static int dimensions = 19;
  List<Pair<Integer, Integer>> similar;
  List<Pair<Integer, Integer>> different;
  Instances normalisedFeatures;

  public static final double eps = 0.05;


  public DistanceMetricOptimization(List<Pair<Integer, Integer>> similar, List<Pair<Integer, Integer>> different, Instances normalisedFeatures) {
    this.similar = similar;
    this.different = different;
    this.normalisedFeatures = normalisedFeatures;
  }

  public Double similarDistanceSumSq(Vector weights) {

    List<Double> weightList = new ArrayList<>();
    double[] weightArr = weights.toArray();
    for(double w : weightArr) weightList.add(w);
    WeightedEuclideanDistance d = new WeightedEuclideanDistance(weightList);

    Double sumSq = 0.0;

    for(Pair<Integer, Integer> similarPair : similar) {
      double distance = d.distance(normalisedFeatures.instance(similarPair.getKey()),
                                   normalisedFeatures.instance(similarPair.getValue()));
      sumSq += distance * distance;
    }

    return sumSq;
  }

  public Double differentDistanceSum(Vector weights) {

    List<Double> weightList = new ArrayList<>();
    double[] weightArr = weights.toArray();
    for(double w : weightArr) weightList.add(w);
    WeightedEuclideanDistance d = new WeightedEuclideanDistance(weightList);

    Double sum = 0.0;

    for(Pair<Integer, Integer> differentPair : different) {
      double distance = d.distance(normalisedFeatures.instance(differentPair.getKey()),
                                   normalisedFeatures.instance(differentPair.getValue()));
      sum += distance;
    }

    return sum;
  }

  public Vector optimize() {

    NewtonRaphsonMinimizer minimizer = new NewtonRaphsonMinimizer(eps, 100);

    C2OptimProblem problem = new C2OptimProblem() {

      // cache function result values since they're used in multiple places
      HashMap<Vector, Double> functionResults = new HashMap<>();

      // Hessian
      @Override
      public RntoMatrix H() {
        RntoMatrix hessian = new RntoMatrix() {

          RealScalarFunction f = f();

          // second order mixed finite difference to construct hessian matrix
          @Override
          public Matrix evaluate(Vector weights) {
            Matrix hess = new DenseMatrix(dimension(), dimension());

            for(int i = 0; i < hess.nRows(); i++) {
              for(int j = 0; j < hess.nCols(); j++) {

                // denote neighbouring function values as pp (+,+), pm (+,-), mp (-,+) ,mm (-,-)
                Vector ppV = weights.deepCopy(); ppV.set(i, ppV.get(i) + eps); ppV.set(j, ppV.get(j) + eps);
                Vector pmV = weights.deepCopy(); pmV.set(i, ppV.get(i) + eps); ppV.set(j, ppV.get(j) - eps);
                Vector mpV = weights.deepCopy(); mpV.set(i, ppV.get(i) - eps); ppV.set(j, ppV.get(j) + eps);
                Vector mmV = weights.deepCopy(); mmV.set(i, ppV.get(i) - eps); ppV.set(j, ppV.get(j) - eps);

                double pp = f.evaluate(ppV);
                double pm = f.evaluate(pmV);
                double mp = f.evaluate(mpV);
                double mm = f.evaluate(mmV);

                double der = (pp - pm - mp + mm) / (4 * eps * eps);

                hess.set(i, j, der);
              }
            }
            return hess;
          }

          @Override
          public int dimensionOfRange() {
            return dimension();
          }

          @Override
          public int dimensionOfDomain() {
            return dimension();
          }
        };
        return hessian;
      }

      // Gradient
      @Override
      public RealVectorFunction g() {
        RealVectorFunction gradient = new RealVectorFunction() {
          @Override
          public Vector evaluate(Vector weights) {

            Vector gradVector = new DenseVector();

            RealScalarFunction f = f();
            double curr = f.evaluate(weights);

            // calculate discrete gradient for each dimension:
            // forward finite difference
            for(int dim = 0; dim < weights.size(); dim++) {
              Vector aheadVector = weights.deepCopy();
              aheadVector.set(dim, aheadVector.get(dim) + eps);
              double ahead = f.evaluate(aheadVector);

              gradVector.set(dim, (ahead - curr) / eps);
            }
            return gradVector;
          }

          @Override
          public int dimensionOfRange() {
            return dimensions;
          }

          @Override
          public int dimensionOfDomain() {
            return dimensions;
          }
        };
        return gradient;
      }

      // Function
      @Override
      public RealScalarFunction f() {

        RealScalarFunction function = new RealScalarFunction() {
          @Override
          public Double evaluate(Vector weights) {

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
          public int dimensionOfRange() {
            return 1;
          }

          @Override
          public int dimensionOfDomain() {
            return dimensions;
          }
        };
        return function;
      }

      @Override
      public int dimension() {
        return dimensions;
      }
    };

    try {
      IterativeMinimizer<Vector> iterativeMinimizer = minimizer.solve(problem);
      Vector minSolution = iterativeMinimizer.minimizer();
      return minSolution;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
