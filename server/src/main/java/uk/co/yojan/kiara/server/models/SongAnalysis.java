package uk.co.yojan.kiara.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

/**
 * A data class representing the results of audio meta-date analysis
 * carried out by The Echo Nest.
 *
 * Each Song has a related SongAnalysis entity that does not need to be
 * loaded for a user-related request.
 *
 * The attributes are used by the Machine Learning module.
 */
@Entity
public class SongAnalysis {

  // Basic Meta Data.
  boolean basicMetaData = false;
  @Id private String id; // Spotify Id
  @Expose private String status;
  @Expose private String artist;
  @Expose private String title;
  @Expose private String release;

  @SerializedName("audio_md5")
  @Expose private String audioMd5;

  @Ignore private SongData songData;


  // Audio Summary.
  boolean audioSummary = false;
  @SerializedName("time_signature")
  @Expose private Integer timeSignature;
  @Expose private Double tempo;
  @Expose private Double energy;
  @Expose private Double liveness;
  @Expose private Double speechiness;
  @Expose private Integer mode;
  @Expose private Double acousticness;
  @Expose private Double danceability;
  @Expose private Integer key;
  @Expose private Double duration;
  @Expose private Double loudness;
  @Expose private Double valence;
  @Expose private Double instrumentalness;
  @SerializedName("analysis_url")
  @Expose private String analysisUrl;


//  @Expose private List<Bar> bars = new ArrayList<Bar>();
//  @Expose private List<Beat> beats = new ArrayList<Beat>();
//  @Expose private List<Tatum> tatums = new ArrayList<Tatum>();
//  @Expose private List<Section> sections = new ArrayList<Section>();
//  @Expose private List<Segment> segments = new ArrayList<Segment>();

  public boolean isBasicMetaData() {
    return basicMetaData;
  }

  public void setBasicMetaData(boolean basicMetaData) {
    this.basicMetaData = basicMetaData;
  }

  public boolean isAudioSummary() {
    return audioSummary;
  }

  public void setAudioSummary(boolean audioSummary) {
    this.audioSummary = audioSummary;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getArtist() {
    return artist;
  }

  public void setArtist(String artist) {
    this.artist = artist;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(String release) {
    this.release = release;
  }

  public String getAudioMd5() {
    return audioMd5;
  }

  public void setAudioMd5(String audioMd5) {
    this.audioMd5 = audioMd5;
  }

  public Integer getTimeSignature() {
    return timeSignature;
  }

  public void setTimeSignature(Integer timeSignature) {
    this.timeSignature = timeSignature;
  }

  public Double getTempo() {
    return tempo;
  }

  public void setTempo(Double tempo) {
    this.tempo = tempo;
  }

  public Double getEnergy() {
    return energy;
  }

  public void setEnergy(Double energy) {
    this.energy = energy;
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

  public Integer getKey() {
    return key;
  }

  public void setKey(Integer key) {
    this.key = key;
  }

  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  public Double getLoudness() {
    return loudness;
  }

  public void setLoudness(Double loudness) {
    this.loudness = loudness;
  }

  public Double getValence() {
    return valence;
  }

  public void setValence(Double valence) {
    this.valence = valence;
  }

  public Double getInstrumentalness() {
    return instrumentalness;
  }

  public void setInstrumentalness(Double instrumentalness) {
    this.instrumentalness = instrumentalness;
  }

  public String getAnalysisUrl() {
    return analysisUrl;
  }

  public void setAnalysisUrl(String analysisUrl) {
    this.analysisUrl = analysisUrl;
  }

  public SongData getSongData() {
    return songData;
  }

  public void setSongData(SongData songData) {
    this.songData = songData;
  }

  /*
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
  } */
}
