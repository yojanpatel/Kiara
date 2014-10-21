package uk.co.yojan.kiara.server.echonest.data;

import com.google.gson.annotations.Expose;

public class Bar {
  @Expose
  private Double start;
  @Expose
  private Double duration;
  @Expose
  private Double confidence;

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
}
