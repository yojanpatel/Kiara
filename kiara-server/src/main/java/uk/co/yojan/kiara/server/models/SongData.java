
package uk.co.yojan.kiara.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Serialize;
import uk.co.yojan.kiara.server.echonest.data.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class SongData {

  @Id private String spotifyId;

  @SerializedName("num_samples")
  @Expose private Integer numSamples;

  @Expose private Double duration;

  @SerializedName("sample_md5")
  @Expose private String sampleMd5;


  @SerializedName("offset_seconds")
  @Expose private Integer offsetSeconds;

  @SerializedName("window_seconds")
  @Expose private Integer windowSeconds;

  @SerializedName("analysis_sample_rate")
  @Expose private Integer analysisSampleRate;

  @SerializedName("analysis_channels")
  @Expose private Integer analysisChannels;

  @SerializedName("end_of_fade_in")
  @Expose private Double endOfFadeIn;

  @SerializedName("start_of_fade_out")
  @Expose private Double startOfFadeOut;

  @Expose private Double loudness;

  @Expose private Double tempo;

  @SerializedName("tempo_confidence")
  @Expose private Double tempoConfidence;

  @SerializedName("time_signature")
  @Expose private Integer timeSignature;

  @SerializedName("time_signature_confidence")
  @Expose private Double timeSignatureConfidence;

  @Expose private Integer key;

  @SerializedName("key_confidence")
  @Expose private Double keyConfidence;

  @Expose private Integer mode;

  @SerializedName("mode_confidence")
  @Expose private Double modeConfidence;

//  @Expose private String codestring;

  /*
  @SerializedName("echoprint_version")
  @Expose private Double echoprintVersion;

  @Expose private String synchstring;

  @SerializedName("synch_version")
  @Expose private Double synchVersion;

  @Expose private String rhythmstring;

  @SerializedName("rhythm_version")
  @Expose private Double rhythmVersion; */

  @Expose @Serialize(zip=true) private List<Bar> bars = new ArrayList<Bar>();
  @Expose @Serialize(zip=true) private List<Beat> beats = new ArrayList<Beat>();
  @Expose @Serialize(zip=true) private List<Tatum> tatums = new ArrayList<Tatum>();
  @Expose @Serialize(zip=true) private List<Section> sections = new ArrayList<Section>();
  @Expose @Serialize(zip=true) private List<Segment> segments = new ArrayList<Segment>();

  public String getSpotifyId() {
    return spotifyId;
  }

  public void setSpotifyId(String spotifyId) {
    this.spotifyId = spotifyId;
  }

  public Integer getNumSamples() {
    return numSamples;
  }

  public void setNumSamples(Integer numSamples) {
    this.numSamples = numSamples;
  }

  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public String getSampleMd5() {
    return sampleMd5;
  }

  public void setSampleMd5(String sampleMd5) {
    this.sampleMd5 = sampleMd5;
  }

  public Integer getOffsetSeconds() {
    return offsetSeconds;
  }

  public void setOffsetSeconds(Integer offsetSeconds) {
    this.offsetSeconds = offsetSeconds;
  }

  public Integer getWindowSeconds() {
    return windowSeconds;
  }

  public void setWindowSeconds(Integer windowSeconds) {
    this.windowSeconds = windowSeconds;
  }

  public Integer getAnalysisSampleRate() {
    return analysisSampleRate;
  }

  public void setAnalysisSampleRate(Integer analysisSampleRate) {
    this.analysisSampleRate = analysisSampleRate;
  }

  public Integer getAnalysisChannels() {
    return analysisChannels;
  }

  public void setAnalysisChannels(Integer analysisChannels) {
    this.analysisChannels = analysisChannels;
  }

  public Double getEndOfFadeIn() {
    return endOfFadeIn;
  }

  public void setEndOfFadeIn(Double endOfFadeIn) {
    this.endOfFadeIn = endOfFadeIn;
  }

  public Double getStartOfFadeOut() {
    return startOfFadeOut;
  }

  public void setStartOfFadeOut(Double startOfFadeOut) {
    this.startOfFadeOut = startOfFadeOut;
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

  public List<Bar> getBars() {
    return bars;
  }

  public void setBars(List<Bar> bars) {
    this.bars = bars;
  }

  public List<Beat> getBeats() {
    return beats;
  }

  public void setBeats(List<Beat> beats) {
    this.beats = beats;
  }

  public List<Tatum> getTatums() {
    return tatums;
  }

  public void setTatums(List<Tatum> tatums) {
    this.tatums = tatums;
  }

  public List<Section> getSections() {
    return sections;
  }

  public void setSections(List<Section> sections) {
    this.sections = sections;
  }

  public List<Segment> getSegments() {
    return segments;
  }

  public void setSegments(List<Segment> segments) {
    this.segments = segments;
  }
}