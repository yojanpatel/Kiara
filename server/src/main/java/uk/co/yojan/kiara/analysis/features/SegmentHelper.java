package uk.co.yojan.kiara.analysis.features;

import uk.co.yojan.kiara.server.echonest.data.Segment;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to slice the data (timbre vectors, pitches) from
 * a list segments, with each element containing a feature vector for the segment to
 * a list of a particular feature for all the segments.
 */
public class SegmentHelper {

  private List<Segment> segments;

  private ArrayList<ArrayList<Double>> pitches;
  private ArrayList<ArrayList<Double>> timbres;
  private ArrayList<Double> loudness;
  private ArrayList<Double> lengths;
  private ArrayList<Double> loudnessProportion;

  private boolean sliced = false;

  public SegmentHelper(List<Segment> segments) {
    this.segments = segments;
    this.pitches = new ArrayList<>();
    for(int i = 0; i < 12; i++) pitches.add(new ArrayList<Double>());
    this.timbres = new ArrayList<>();
    for(int i = 0; i < 12; i++) timbres.add(new ArrayList<Double>());
  }

  /*
   * Called lazily when a timbres or pitches is requested.
   * Essentially traverses all the segments once and reformats
   * the timbre/pitch vectors so each component has its own list
   * consisting of the components values over all the segments.
   *
   * Vertical -> Horizontal slicing.
   */
  private void slice() {
    for(Segment segment : segments) {
      /* a 12-vector for pitch probabilities for the current segment. */
      List<Double> pitchSegment = segment.getPitches();
      for(int i = 0; i < pitchSegment.size(); i++) {
        pitches.get(i).add(pitchSegment.get(i));
      }

      /* a 14-vector for the timbre components for the current segment. */
      List<Double> timbreSegment = segment.getTimbre();
      for(int i = 0; i < timbreSegment.size(); i++) {
        timbres.get(i).add(timbreSegment.get(i));
      }

//      loudness.add(segment.getLoudnessMax());
//      lengths.add(segment.getDuration());
//      loudnessProportion.add(segment.getLoudnessMaxTime() / segment.getDuration());
    }
    sliced = true;
  }

  public ArrayList<ArrayList<Double>> getPitches() {
    if(!sliced) slice();
    return pitches;
  }

  public ArrayList<ArrayList<Double>> getTimbres() {
    if(!sliced) slice();
    return timbres;
  }

  public ArrayList<Double> getLoudness() {
    if(!sliced) slice();
    return loudness;
  }

  public void setLoudness(ArrayList<Double> loudness) {
    this.loudness = loudness;
  }

  public ArrayList<Double> getLengths() {
    if(!sliced) slice();
    return lengths;
  }

  public void setLengths(ArrayList<Double> lengths) {
    this.lengths = lengths;
  }

  public ArrayList<Double> getLoudnessProportion() {
    return loudnessProportion;
  }

  public void setLoudnessProportion(ArrayList<Double> loudnessProportion) {
    this.loudnessProportion = loudnessProportion;
  }
}
