package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.features.*;
import uk.co.yojan.kiara.server.echonest.data.Bar;
import uk.co.yojan.kiara.server.echonest.data.Section;
import uk.co.yojan.kiara.server.echonest.data.Segment;
import uk.co.yojan.kiara.server.echonest.data.Tatum;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.SongData;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


public class FeatureExtractionTask implements DeferredTask {

  private String spotifyId;
  private SongData songData;
  private SongAnalysis songAnalysis;

  public FeatureExtractionTask(String spotifyId) {
    this.spotifyId = spotifyId;
  }

  private static final Logger log = Logger.getLogger(FeatureExtractionTask.class.getName());

  @Override
  public void run() {
    songData = ofy().load().key(Key.create(SongData.class, spotifyId)).now();
    songAnalysis = ofy().load().key(Key.create(SongAnalysis.class, spotifyId)).now();

    SongFeature sf = new SongFeature();
    sf.setId(spotifyId);
    if(songData == null) {
      Logger.getLogger("").warning(spotifyId + " - associated songdata not found.");
      return;
    } else {
      if(songData.getSegments() == null) {
        Logger.getLogger("").warning(spotifyId + " - associated segments not found.");
        return;
      }
    }

    ArrayList<Tatum> tatums = new ArrayList<>(songData.getTatums());
    ArrayList<Bar> bars = new ArrayList<>(songData.getBars());
    ArrayList<Section> sections = new ArrayList<>(songData.getSections());
    List<Segment> segments = songData.getSegments();
//    List<Segment> loudSegments = getLoudestSectionSegments(sections, segments);

    if(segments.get(0).getLoudnessMax() == null) log.warning(spotifyId + " - loudnessmax is null.");

//    SegmentHelper loudHelper = new SegmentHelper(loudSegments);
    SegmentHelper allSegmentHelper = new SegmentHelper(segments);

//    ArrayList<ArrayList<Double>> loudTimbres = loudHelper.getTimbres();

    sf.setTimbreMoments(vectorStatMoments(allSegmentHelper.getTimbres()));
///   sf.setLoudSegmentTimbreMoments(vectorStatMoments(loudTimbres));
//    sf.setTimbreAreaMoments(new AreaMomentTimbre(loudSegments).get());

    ArrayList<ArrayList<Double>> pitches = allSegmentHelper.getPitches();
    sf.setPitchMoments(vectorStatMoments(pitches));


    sf.setDuration(songData.getDuration());
    sf.setEnergy(songAnalysis.getEnergy());
    sf.setValence(songAnalysis.getValence());
    sf.setLoudness(songData.getLoudness());
    sf.setTempo(songData.getTempo());
    sf.setNormalisedTempo(normaliseTempo(songData.getTempo()));
    sf.setTempoConfidence(songData.getTempoConfidence());


    // restricted by app engine.
//    double[] samples;
//    try {
//      samples = DownloadPreview.download(sf.getId());
//      MFCCAreaMoment mfcc = new MFCCAreaMoment(samples);
//      ArrayList<Double> averages = mfcc.averageMoments();
//      sf.setTimbreAreaMoments(averages);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }


    // Compute stats regarding tatum length
    TatumHelper tatumHelper = new TatumHelper(tatums);
    Statistics tatumDurationStats = new Statistics(tatumHelper.getDuration());
    sf.setTatumLengthMean(tatumDurationStats.mean());
    sf.setTatumLengthVar(tatumDurationStats.variance());

    // Compute stats regarding bars
    Statistics barDurationStats = new Statistics(new BarHelper(bars).getDuration());
    sf.setBarLengthMean(barDurationStats.mean());
    sf.setBarLengthVar(barDurationStats.variance());

    SectionHelper sectionHelper = new SectionHelper(sections);
    Statistics sectionDurationStats = new Statistics(sectionHelper.getDuration());
    sf.setSectionLengthMean(sectionDurationStats.mean());

    Statistics sectionTempoStats = new Statistics(sectionHelper.getTempo());
    sf.setMaxSectionTempo(sectionTempoStats.max());
    sf.setMinSectionTempo(sectionTempoStats.min());

    sf.setInitialLoudness(sectionHelper.initialLoudness());
    sf.setInitialTempo(sectionHelper.initialTempo());
    sf.setFinalLoudness(sectionHelper.finalLoudness());
    sf.setFinalTempo(sectionHelper.finalTempo());

    SegmentHelper initialHelper;
    SegmentHelper finalHelper;
    if (sections.size() > 1) {
      initialHelper = new SegmentHelper(getInitial(segments, sections.get(1).getStart()));
      sf.setInitialTimbreMoments(vectorStatMoments(initialHelper.getTimbres()));
      finalHelper = new SegmentHelper(getFinal(segments, sections.get(sections.size() - 1).getStart()));
      sf.setFinalTimbreMoments(vectorStatMoments(finalHelper.getTimbres()));
    } else {
      sf.setInitialTimbreMoments(sf.getTimbreMoments());
    }

    sf.setAcousticness(songAnalysis.getAcousticness());
    sf.setDanceability(songAnalysis.getDanceability());
    sf.setLiveness(songAnalysis.getLiveness());
    sf.setSpeechiness(songAnalysis.getSpeechiness());
    sf.setInstrumentalness(songAnalysis.getInstrumentalness());

//    ofy().delete().entities(songAnalysis, songData);
    ofy().save().entity(sf);
  }

  /*
   * Given N lists of features (e.g. 12 lists for the pitches), calls the Statistics class
   * for each of the lists (i.e. for each pitch from 0-11) and constructs the stat moments vector
   * before returning as a 2D ArrayList.
   */
  private ArrayList<ArrayList<Double>> vectorStatMoments(ArrayList<ArrayList<Double>> features) {
    ArrayList<ArrayList<Double>> statMoments = new ArrayList<>();

    for(ArrayList<Double> featureList : features) {
      Statistics stats = new Statistics(featureList);
      statMoments.add(stats.momentVector());
    }
    return statMoments;
  }

  private ArrayList<Double> vectorStateMoments(ArrayList<Double> feature) {
    Statistics stats = new Statistics(feature);
    return stats.momentVector();
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

  private List<Segment> getInitial(List<Segment> segments, double end) {
    for(int i = 0; i < segments.size(); i++) {
      Segment segment = segments.get(i);
      if(segment.getStart() + segment.getDuration() > end) {
        return new ArrayList<>(segments.subList(0, i));
      }
    }
    return null;
  }

  private List<Segment> getFinal(List<Segment> segments, double start) {
    for(int i = segments.size() - 1; i >= 0; i--) {
      Segment segment = segments.get(i);
      if(segment.getStart() + segment.getDuration() < start) {
        return new ArrayList<>(segments.subList(i, segments.size()));
      }
    }
    return null;
  }

  private ArrayList<Segment> getLoudestSectionSegments(List<Section> sections, List<Segment> segments) {
    // get loudest section time range
    double start = 0;
    double end = 0;
    double currentLoudest = Double.NEGATIVE_INFINITY;
    int i = 0;
    for(; i < sections.size(); i++) {
      Section section = sections.get(i);
      if(section.getLoudness() > currentLoudest) {
        currentLoudest = section.getLoudness();
        start = section.getStart();
        end = section.getStart() + section.getDuration();
      }
    }

    // extend range towards the end until 30 seconds reached
    int j = i + 1;
    while(end - start < 30 && j < sections.size()) {
      Section section = sections.get(j++);
      end = section.getStart() + section.getDuration();
    }

    // extend range towards the start if needed
    int k = i - 1;
    while(end - start < 30 && k >= 0) {
      Section section = sections.get(k--);
      start = section.getStart();
    }

    // filter
    ArrayList<Segment> loudSegments = new ArrayList<>();

    int startIndex = binarySearch(segments, start);
    startIndex = startIndex < 0 ? 0 : startIndex;

    for(; startIndex < segments.size(); startIndex++) {
      Segment segment = segments.get(startIndex);
      if(start <= segment.getStart()) {
        if(segment.getStart() < end) {
          loudSegments.add(segment);
        } else {
          break;
        }
      }
    }

    return loudSegments;
  }


  private int binarySearch(List<Segment> segments, double start) {

    int lo = 0;
    int hi = segments.size();

    while(lo <= hi) {
      int mid =  lo + (hi - lo)/ 2;
      Segment seg = segments.get(mid);
      if(match(segments, mid, start)) {
        return mid;
      } else if(seg.getStart() < start) {
        lo = mid + 1;
      } else {
        hi = mid - 1;
      }
    }

    return -1;
  }

  private boolean match(List<Segment> segments, int index, double start) {
    // first element => match if section started before (shouldn't really happen).
    if(index == 0) return start <= segments.get(index).getStart();

    // last element => match if section started after (shouldn't really happen).
    if(index == segments.size() - 1) return segments.get(index).getStart() <= start;

    // else if section started between the last and next segment (i.e. this one).
    return segments.get(index - 1).getStart() < start && start < segments.get(index + 1).getStart();
  }
}
