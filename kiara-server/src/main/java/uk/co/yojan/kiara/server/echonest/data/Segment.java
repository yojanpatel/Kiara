package uk.co.yojan.kiara.server.echonest.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Segment {
  @Expose
  private Double start;
  @Expose
  private Double duration;
  @Expose
  private Double confidence;
  @SerializedName("loudness_start")
  @Expose
  private Double loudnessStart;
  @SerializedName("loudness_max_time")
  @Expose
  private Double loudnessMaxTime;
  @SerializedName("loudness_max")
  @Expose
  private Double loudnessMax;
  @Expose
  private List<Double> pitches = new ArrayList<Double>();
  @Expose
  private List<Double> timbre = new ArrayList<Double>();

  public Double getStart() {
    return start;
  }

  public void setStart(Double start) {
    this.start = start;
  }

  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public Double getConfidence() {
    return confidence;
  }

  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  public Double getLoudnessStart() {
    return loudnessStart;
  }

  public void setLoudnessStart(Double loudnessStart) {
    this.loudnessStart = loudnessStart;
  }

  public Double getLoudnessMaxTime() {
    return loudnessMaxTime;
  }

  public void setLoudnessMaxTime(Double loudnessMaxTime) {
    this.loudnessMaxTime = loudnessMaxTime;
  }

  public Double getLoudnessMax() {
    return loudnessMax;
  }

  public void setLoudnessMax(Double loudnessMax) {
    this.loudnessMax = loudnessMax;
  }

  public List<Double> getPitches() {
    return pitches;
  }

  public void setPitches(List<Double> pitches) {
    this.pitches = pitches;
  }

  public List<Double> getTimbre() {
    return timbre;
  }

  public void setTimbre(List<Double> timbre) {
    this.timbre = timbre;
  }

}
