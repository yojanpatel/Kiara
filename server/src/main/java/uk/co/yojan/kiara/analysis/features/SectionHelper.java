package uk.co.yojan.kiara.analysis.features;

import uk.co.yojan.kiara.server.echonest.data.Section;

import java.util.ArrayList;

/**
 * Created by yojan on 12/29/14.
 */
public class SectionHelper {

  private boolean processed;

  private ArrayList<Section> sections;
  private ArrayList<Double> tempo;
  private ArrayList<Double> loudness;
  private ArrayList<Double> duration;

  public SectionHelper(ArrayList<Section> sections) {
    this.sections = sections;
    tempo = new ArrayList<>();
    loudness = new ArrayList<>();
    duration = new ArrayList<>();
  }

  private void process() {
    for(Section section : sections) {
      tempo.add(section.getTempo());
      loudness.add(section.getLoudness());
      duration.add(section.getDuration());
    }
    processed = true;
  }

  public ArrayList<Double> getTempo() {
    if(!processed) process();
    return tempo;
  }
  public ArrayList<Double> getLoudness() {
    if(!processed) process();
    return loudness;
  }
  public ArrayList<Double> getDuration() {
    if(!processed) process();
    return duration;
  }

  public Double initialTempo() {
    return initialTempo(0.01);
  }

  public Double initialLoudness() {
    return initialLoudness(0.01);
  }

  public Double finalTempo() {
    return finalTempo(0.01);
  }

  public Double finalLoudness() {
    return finalLoudness(0.01);
  }

    // threshold - multiple sections used if they are below threshold seconds
  public Double initialTempo(double threshold) {
    double len = 0;
    double tempoTotal = 0;
    int i = 0;
    for(; i < sections.size() && len < threshold; i++) {
      Section section = sections.get(i);
      tempoTotal += section.getTempo();

      len += section.getDuration();
    }

    return tempoTotal / i;
  }

  public Double initialLoudness(double threshold) {
    double len = 0;
    double loudnessTotal = 0;
    int i = 0;
    for(; i < sections.size() && len < threshold; i++) {
      Section section = sections.get(i);
      loudnessTotal += section.getLoudness();

      len += section.getDuration();
    }

    return loudnessTotal / i;
  }

  public Double finalTempo(double threshold) {
    double len = 0;
    double tempoTotal = 0;
    int i = 0;
    for(; i < sections.size() && len < threshold; i++) {
      Section section = sections.get(sections.size() - i - 1);
      tempoTotal += section.getTempo();

      len += section.getDuration();
    }

    return tempoTotal / i;
  }

  public Double finalLoudness(double threshold) {
    double len = 0;
    double loudnessTotal = 0;
    int i = 0;
    for(; i < sections.size() && len < threshold; i++) {
      Section section = sections.get(sections.size() - i - 1);
      loudnessTotal += section.getLoudness();

      len += section.getDuration();
    }
    return loudnessTotal / i;
  }


}
