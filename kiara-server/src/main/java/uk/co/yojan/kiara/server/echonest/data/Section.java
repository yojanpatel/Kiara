package uk.co.yojan.kiara.server.echonest.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Section {
  @Expose
  private Double start;
  @Expose
  private Double duration;
  @SerializedName("tempo_confidenceidence")
  @Expose
  private Double tempoConfidenceidence;
  @Expose
  private Double loudness;
  @Expose
  private Double tempo;
  @SerializedName("tempo_confidence")
  @Expose
  private Double tempoConfidence;
  @Expose
  private Integer key;
  @SerializedName("key_confidence")
  @Expose
  private Double keyConfidence;
  @Expose
  private Integer mode;
  @SerializedName("mode_confidence")
  @Expose
  private Double modeConfidence;
  @SerializedName("time_signature")
  @Expose
  private Integer timeSignature;
  @SerializedName("time_signature_confidence")
  @Expose
  private Double timeSignatureConfidence;

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

  public Double getTempoConfidenceidence() {
    return tempoConfidenceidence;
  }

  public void setTempoConfidenceidence(Double tempoConfidenceidence) {
    this.tempoConfidenceidence = tempoConfidenceidence;
  }

  public Double getLoudness() {
    return loudness;
  }

  public void setLoudness(Double loudness) {
    this.loudness = loudness;
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

  public Integer getKey() {
    return key;
  }

  public void setKey(Integer key) {
    this.key = key;
  }

  public Double getKeyConfidence() {
    return keyConfidence;
  }

  public void setKeyConfidence(Double keyConfidence) {
    this.keyConfidence = keyConfidence;
  }

  public Integer getMode() {
    return mode;
  }

  public void setMode(Integer mode) {
    this.mode = mode;
  }

  public Double getModeConfidence() {
    return modeConfidence;
  }

  public void setModeConfidence(Double modeConfidence) {
    this.modeConfidence = modeConfidence;
  }

  public Integer getTimeSignature() {
    return timeSignature;
  }

  public void setTimeSignature(Integer timeSignature) {
    this.timeSignature = timeSignature;
  }

  public Double getTimeSignatureConfidence() {
    return timeSignatureConfidence;
  }

  public void setTimeSignatureConfidence(Double timeSignatureConfidence) {
    this.timeSignatureConfidence = timeSignatureConfidence;
  }
}
