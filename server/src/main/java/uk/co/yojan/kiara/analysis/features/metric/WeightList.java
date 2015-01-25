package uk.co.yojan.kiara.analysis.features.metric;

import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class to help manage feature weights.
 */
public class WeightList {

  private ArrayList<Double> weights;
  private List<String> attNames;

  public WeightList() {
    weights = new ArrayList<>();
    attNames = SongFeature.getFeatureNames();

    // initialize equally weighted
    for(int i = 0; i < attNames.size(); i++) {
      weights.add(1.0);
    }
  }

  public void setStatisticWeights(List<Double> statisticWeights) {
    assert statisticWeights.size() == 8;
    for(int t = 0; t < 12; t++) {
      for(int s = 0; s < 8; s++) {
        int index = t * 8 + s;
        weights.set(index, weights.get(index) * statisticWeights.get(s));
      }
    }
  }

  public void setTimbreWeights(List<Double> timbreWeights) {
    assert timbreWeights.size() == 12;

    for(int t = 0; t < 12; t++) {
      for(int s = 0; s < 8; s++) {
        int index = t * 8 + s;
        weights.set(index, weights.get(index) * timbreWeights.get(t));
      }
    }
  }

  /* attName memberof
   * {tempo, tempoConfidence, loudness, energy, valence, tatumLengthMean, tatumLengthVar}
   */
  public void setAttWeight(String attName, Double weight) {
    int index = attNames.indexOf(attName);

    if(index != -1) {
      weights.set(index, weight);
    }
  }

  public void setAttWeights(List<Double> weights) {
    setAttWeight("tempo", weights.get(0));
    setAttWeight("tempoConfidence", weights.get(1));
    setAttWeight("loudness", weights.get(2));
    setAttWeight("energy", weights.get(3));
    setAttWeight("valence", weights.get(4));
    setAttWeight("tatumLengthMean", weights.get(5));
    setAttWeight("tatumLengthVar", weights.get(6));
  }

  public ArrayList<Double> getWeights() {
    return weights;
  }

  public ArrayList<Double> get() {
    normalize();
    return weights;
  }

  public void normalize() {
    Double max = Collections.max(weights);
    for(int i = 0; i < weights.size(); i++) {
      weights.set(i, weights.get(i) / max);
    }
  }

  /** Static Utils **/

  // Currently optimising with respect to Timbre Means, and the other
  // meta features
//  public static ArrayList<Double> convert(Vector vector) {
//    // 12 timbre, 7 meta features as above.
//    assert vector.size() == (12 + 7);
//
//    WeightList weightList = new WeightList();
//
//    List<Double> timbreWeights = new ArrayList<>();
//    for(int i = 0; i < 12; i++) {
//      timbreWeights.add(vector.get(i));
//    }
//
//    List<Double> attWeights = new ArrayList<>();
//    for(int i = 12; i < vector.size(); i++) {
//      attWeights.add(vector.get(i));
//    }
//
//    weightList.setTimbreWeights(timbreWeights);
//    weightList.setAttWeights(attWeights);
//    weightList.normalize();
//
//    return weightList.get();
//  }
//
  public static List<Double> convert(List<Double> weights) {
    List<Double> vector = new ArrayList<>();

    for(int i = 0; i < 12; i++) {
      for(int j = 0; j < 8; j++) {
        vector.add(weights.get(i));
      }
    }

    for(int i = 12; i < weights.size(); i++) {
      vector.add(weights.get(i));
    }
    assert vector.size() > 96;
    return  vector;
//    return vector.toArray(new Double[vector.size()]);
  }

  public static void normalize(List<Double> weights) {
    Double max = Collections.max(weights);
    for(int i = 0; i < weights.size(); i++) {
      weights.set(i, weights.get(i) / max);
    }
  }
}
