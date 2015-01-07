package uk.co.yojan.kiara.server.models;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SongFeatureTest extends TestCase {

  @Test
  public void testGetFeaturesNames() {
    SongFeature s = new SongFeature();
    s.setTempoConfidence(1.0);
    s.setNormalisedTempo(2.0);
    s.setLoudness(3.0);
    s.setEnergy(4.0);
    s.setValence(5.0);
    ArrayList<ArrayList<Double>> timbres = new ArrayList<>();
    ArrayList<ArrayList<Double>> pitches = new ArrayList<>();
    for(int i = 0; i < 12; i++) {
      timbres.add(i, new ArrayList<Double>());
      pitches.add(i, new ArrayList<Double>());
      for(int j = 0; j < 8; j++) {
        timbres.get(i).add(j, j + 20.0);
        pitches.get(i).add(j, j + 40.0);
      }
    }
    s.setTimbreMoments(timbres);
    s.setPitchMoments(pitches);
    List<String> featureNames = SongFeature.getFeatureNames();
    try {
      double[] featureVals = s.getFeatureValues();
      for(int i = 0; i < featureVals.length; i++) {
        System.out.println(featureNames.get(i) + ": " + featureVals[i]);
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

}