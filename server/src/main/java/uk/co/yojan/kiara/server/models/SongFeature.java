package uk.co.yojan.kiara.server.models;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;
import uk.co.yojan.kiara.server.annotations.Feature;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a DataStore entity, that contains the features for the song
 * to allow clustering.
 *
 * The choice of the feature set to be used is TEN - Temporal Echonest Features
 * All statistical moments of the song's segments:
 * -- Pitches, Timbre, Loudness Max, Loudness Max Time, lengths.
 *
 */
@Entity
public class SongFeature {

  @Id private String id; // Spotify Id

  /* 12 pitches * 8 statistical moments */
  @Serialize(zip=true)
//  @Feature(dims = 2, size = {12, 8}, weight = 0.5)
  private ArrayList<ArrayList<Double>> pitchMoments;

  /* 14 timbre components * 8 statistical moments */
  @Serialize(zip=true)
  @Feature(dims = 2, size = {12, 8})
  private ArrayList<ArrayList<Double>> timbreMoments;

  @Serialize(zip=true)
//  @Feature(size = {9})
  private ArrayList<Double> timbreAreaMoments;

  @Serialize(zip=true)
//  @Feature(dims = 2, size = {12, 8})
  private ArrayList<ArrayList<Double>> loudSegmentTimbreMoments;


  private Double duration;
  @Feature private Double tempo;
  private Double normalisedTempo;
  @Feature private Double tempoConfidence;
  @Feature private Double loudness;
  @Feature private Double energy;
  @Feature private Double valence;

  // A bar (or measure) is a segment of time defined as a given number of beats.
  // Bar offsets also indicate downbeats, the first beat of the measure.
//  @Feature
  private Double barLengthMean;
//  @Feature
  @Feature private Double barLengthVar;

  // Tatums represent the lowest regular pulse train that a listener intuitively
  // infers from the timing of perceived musical events (segments).
//  @Feature
  @Feature private Double tatumLengthMean;
  @Feature private Double tatumLengthVar;

  // Sections are defined by large variations in rhythm or timbre.
  private Double sectionLengthMean;
  private Double maxSectionTempo;
  private Double minSectionTempo;

  private Double liveness;
  private Double speechiness;
  private Integer mode;
  private Double acousticness;
  private Double danceability;
  private Double instrumentalness;

  // features to be used to order songs when a cluster has been located.
  private Double initialTempo;
  private Double initialLoudness;
  @Serialize(zip=true)
  private ArrayList<ArrayList<Double>> initialTimbreMoments;

  private Double finalTempo;
  private Double finalLoudness;
  @Serialize(zip=true)
  private ArrayList<ArrayList<Double>> finalTimbreMoments;


  @Serialize(zip = true)
  private ArrayList<Double> lengths;



  public SongFeature() {
    pitchMoments = new ArrayList<>();
    timbreMoments = new ArrayList<>();
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ArrayList<ArrayList<Double>> getPitchMoments() {
    return pitchMoments;
  }

  public void setPitchMoments(ArrayList<ArrayList<Double>> pitchMoments) {
    this.pitchMoments = pitchMoments;
  }

  public ArrayList<ArrayList<Double>> getTimbreMoments() {
    return timbreMoments;
  }

  public void setTimbreMoments(ArrayList<ArrayList<Double>> timbreMoments) {
    this.timbreMoments = timbreMoments;
  }

  /*
   * ArrayList of the statistical moments for a given pitch.
   *
   * @param pitch  an int between 0-11 inclusive corresponding to the pitch.
   * @return  ArrayList<Double> that correspond to the statistical moments.
   */
  public ArrayList<Double> getPitchMoment(int pitch) {
    assert 0 <= pitch && pitch < 12;
    return pitchMoments.get(pitch);
  }

  /*
   * ArrayList of the statistical moments for a given timbre.
   *
   * @param timbre  an int between 0-13 inclusive corresponding to the timbre base function.
   * @return  ArrayList<Double> that correspond to the statistical moments.
   */
  public ArrayList<Double> getTimbreMoment(int timbre) {
    assert 0 <= timbre && timbre < 14;
    return timbreMoments.get(timbre);
  }

  public ArrayList<Double> getTimbreAreaMoments() {
    return timbreAreaMoments;
  }

  public void setTimbreAreaMoments(ArrayList<Double> timbreAreaMoments) {
    this.timbreAreaMoments = timbreAreaMoments;
  }

  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public Double getTempo() {
    return tempo;
  }

  public void setTempo(Double tempo) {
    this.tempo = tempo;
  }

  public Double getTempoConfidence() {
    return tempoConfidence;
  }

  public void setTempoConfidence(Double tempoConfidence) {
    this.tempoConfidence = tempoConfidence;
  }

  public Double getNormalisedTempo() {
    return normalisedTempo;
  }

  public void setNormalisedTempo(Double normalisedTempo) {
    this.normalisedTempo = normalisedTempo;
  }

  public Double getEnergy() {
    return energy;
  }

  public void setEnergy(Double energy) {
    this.energy = energy;
  }

  public Double getValence() {
    return valence;
  }

  public void setValence(Double valence) {
    this.valence = valence;
  }

  /*
   * Euclidian distance metric to another Song.
   * Sum for all features, the component-wise difference squared.
   */
  public Double distanceTo(SongFeature other) {
    Double distance = 0.0;

    distance += Math.pow((other.getTempo() - this.getTempo()), 2);
    distance += Math.pow((other.getEnergy() - this.getEnergy()), 2);
    distance += Math.pow((other.getValence() - this.getValence()), 2);
    distance += Math.pow((other.getLoudness() - this.getLoudness()), 2);

    for(int pitch = 0; pitch < pitchMoments.size(); pitch++) {
      ArrayList<Double> thisPitchVector = this.getPitchMoment(pitch);
      ArrayList<Double> otherPitchVector = other.getPitchMoment(pitch);
      for(int stat = 0; stat < thisPitchVector.size(); stat++) {
        distance += Math.pow(otherPitchVector.get(stat) - thisPitchVector.get(stat), 2);
      }
    }

    for(int timbre = 0; timbre < timbreMoments.size(); timbre++) {
      ArrayList<Double> thisTimbreVector = this.getTimbreMoment(timbre);
      ArrayList<Double> otherTimbreVector = other.getTimbreMoment(timbre);
      for(int stat = 0; stat < thisTimbreVector.size(); stat++) {
        distance += Math.pow(otherTimbreVector.get(stat) - thisTimbreVector.get(stat), 2);
      }
    }

    return distance;
  }

  public static List<String> getFeatureNames() {
    ArrayList<String> featureNames = new ArrayList<>();
    for(Field field : SongFeature.class.getDeclaredFields()) {
      Feature featureAnnotation = field.getAnnotation(Feature.class);
      if(featureAnnotation != null) {
        if(featureAnnotation.dims() == 1) {
          /* Scalar */
          if(featureAnnotation.size()[0] == 1) {
            featureNames.add(field.getName());
          }
          /* 1D Vector */
          else {
            for(int i = 0; i < featureAnnotation.size()[0]; i++) {
              featureNames.add(field.getName() + "-" + i);
            }
          }
        }/* 2D Matrix */
        else if(featureAnnotation.dims() == 2) {
          System.out.println(featureAnnotation.size()[0] + "x" + featureAnnotation.size()[1]);
          for(int i = 0; i < featureAnnotation.size()[0]; i++) {
            for(int j = 0; j < featureAnnotation.size()[1]; j++) {
              featureNames.add(field.getName() + "-" + i + "-" + j);
            }
          }
        }
      }
    }
    return featureNames;
  }

  public double[] getFeatureValues() throws IllegalAccessException {
    Field[] fields = SongFeature.class.getDeclaredFields();
    /* TODO: remove */
    setNormalisedTempo(normaliseTempo(getTempo()));

    /** Count the number of features. */
    int numFeatures = 0;
    for(Field field : fields) {
      Feature featureAnnotation = field.getAnnotation(Feature.class);
      if(featureAnnotation != null) {
        if(featureAnnotation.dims() == 1) {
          numFeatures += featureAnnotation.size()[0];
        } else if(featureAnnotation.dims() == 2) {
          numFeatures += featureAnnotation.size()[0]*featureAnnotation.size()[1];
        }
      }
    }

    /** Construct the feature value double. */
    double[] featureVals = new double[numFeatures];
    int featureIndex = 0;
    for(Field field : SongFeature.class.getDeclaredFields()) {
      Feature featureAnnotation = field.getAnnotation(Feature.class);
      if(featureAnnotation != null) {
        if(featureAnnotation.dims() == 1) {
          /* Scalar */
          if(featureAnnotation.size()[0] == 1) {
            Double val = (Double)field.get(this);
            if(val == null)
              featureVals[featureIndex] = 0;
            else
              featureVals[featureIndex] = featureAnnotation.weight() * val;
            featureIndex++;
          }
          /* 1D Vector */
          else {
            for(int i = 0; i < featureAnnotation.size()[0]; i++) {
              featureVals[featureIndex] = featureAnnotation.weight() * ((ArrayList<Double>)field.get(this)).get(i);
              featureIndex++;
            }
          }
        }/* 2D Matrix */
        else if(featureAnnotation.dims() == 2) {
          for(int i = 0; i < featureAnnotation.size()[0]; i++) {
            for(int j = 0; j < featureAnnotation.size()[1]; j++) {
              ArrayList<ArrayList<Double>> list = ((ArrayList<ArrayList<Double>>)field.get(this));
              featureVals[featureIndex] = featureAnnotation.weight() * list.get(i).get(j);
              featureIndex++;
            }
          }
        }
      }
    }
    return featureVals;
  }

  public Double getBarLengthMean() {
    return barLengthMean;
  }

  public void setBarLengthMean(Double barLengthMean) {
    this.barLengthMean = barLengthMean;
  }

  public Double getBarLengthVar() {
    return barLengthVar;
  }

  public void setBarLengthVar(Double barLengthVar) {
    this.barLengthVar = barLengthVar;
  }

  public Double getTatumLengthMean() {
    return tatumLengthMean;
  }

  public void setTatumLengthMean(Double tatumLengthMean) {
    this.tatumLengthMean = tatumLengthMean;
  }

  public Double getTatumLengthVar() {
    return tatumLengthVar;
  }

  public void setTatumLengthVar(Double tatumLengthVar) {
    this.tatumLengthVar = tatumLengthVar;
  }

  public Double getSectionLengthMean() {
    return sectionLengthMean;
  }

  public void setSectionLengthMean(Double sectionLengthMean) {
    this.sectionLengthMean = sectionLengthMean;
  }

  public Double getMaxSectionTempo() {
    return maxSectionTempo;
  }

  public void setMaxSectionTempo(Double maxSectionTempo) {
    this.maxSectionTempo = maxSectionTempo;
  }


  public Double getMinSectionTempo() {
    return minSectionTempo;
  }

  public void setMinSectionTempo(Double minSectionTempo) {
    this.minSectionTempo = minSectionTempo;
  }

  public Double getInitialTempo() {
    return initialTempo;
  }

  public void setInitialTempo(Double initialTempo) {
    this.initialTempo = initialTempo;
  }

  public Double getInitialLoudness() {
    return initialLoudness;
  }

  public void setInitialLoudness(Double initialLoudness) {
    this.initialLoudness = initialLoudness;
  }

  public Double getFinalTempo() {
    return finalTempo;
  }

  public void setFinalTempo(Double finalTempo) {
    this.finalTempo = finalTempo;
  }

  public Double getFinalLoudness() {
    return finalLoudness;
  }

  public void setFinalLoudness(Double finalLoudness) {
    this.finalLoudness = finalLoudness;
  }

  public ArrayList<ArrayList<Double>> getFinalTimbreMoments() {
    return finalTimbreMoments;
  }

  public void setFinalTimbreMoments(ArrayList<ArrayList<Double>> finalTimbreMoments) {
    this.finalTimbreMoments = finalTimbreMoments;
  }

  public ArrayList<ArrayList<Double>> getInitialTimbreMoments() {
    return initialTimbreMoments;
  }

  public void setInitialTimbreMoments(ArrayList<ArrayList<Double>> initialTimbreMoments) {
    this.initialTimbreMoments = initialTimbreMoments;
  }

  public Double getLiveness() {
    return liveness;
  }

  public void setLiveness(Double liveness) {
    this.liveness = liveness;
  }

  public Double getSpeechiness() {
    return speechiness;
  }

  public void setSpeechiness(Double speechiness) {
    this.speechiness = speechiness;
  }

  public Integer getMode() {
    return mode;
  }

  public void setMode(Integer mode) {
    this.mode = mode;
  }

  public Double getAcousticness() {
    return acousticness;
  }

  public void setAcousticness(Double acousticness) {
    this.acousticness = acousticness;
  }

  public Double getDanceability() {
    return danceability;
  }

  public void setDanceability(Double danceability) {
    this.danceability = danceability;
  }

  public Double getInstrumentalness() {
    return instrumentalness;
  }

  public void setInstrumentalness(Double instrumentalness) {
    this.instrumentalness = instrumentalness;
  }

  public ArrayList<Double> getLengths() {
    return lengths;
  }

  public void setLengths(ArrayList<Double> lengths) {
    this.lengths = lengths;
  }


  public Double getLoudness() {
    return loudness;
  }

  public void setLoudness(Double loudness) {
    this.loudness = loudness;
  }

  public ArrayList<ArrayList<Double>> getLoudSegmentTimbreMoments() {
    return loudSegmentTimbreMoments;
  }

  public void setLoudSegmentTimbreMoments(ArrayList<ArrayList<Double>> loudSegmentTimbreMoments) {
    this.loudSegmentTimbreMoments = loudSegmentTimbreMoments;
  }

  /**
   * Normalises the tempo range [0, 500] according to EchoNest to [0, 1]
   * based on a logarithmic scale.
   *
   * f(tempo) = (1 - cos(pi*x / 500))/2, where K is a normalisation constant.
   *
   * @return a Double between 0 and 1 representing the normalised tempo feature.
   */
  private Double normaliseTempo(Double tempo) {
    return (1 - Math.cos(Math.PI * tempo / 500)) / 2;
  }

  private Double normaliseUnbounded(Double num) {
    return 0.0;
  }

  public static void main(String[] args) {
    System.out.println(getFeatureNames());
  }
}
