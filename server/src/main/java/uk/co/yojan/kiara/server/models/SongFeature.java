package uk.co.yojan.kiara.server.models;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;

import java.util.ArrayList;

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
  @Serialize(zip=true) private ArrayList<ArrayList<Double>> pitchMoments;

  /* 14 timbre components * 8 statistical moments */
  @Serialize(zip=true) private ArrayList<ArrayList<Double>> timbreMoments;

  private Double duration;
  private Double tempo;
  private Double tempoConfidence;
  private Double loudness;
  private Double energy;
  private Double valence;


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

  public Double getLoudness() {
    return loudness;
  }

  public void setLoudness(Double loudness) {
    this.loudness = loudness;
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
}
