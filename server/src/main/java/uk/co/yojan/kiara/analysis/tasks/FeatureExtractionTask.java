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

    ArrayList<Segment> segments = new ArrayList<>(songData.getSegments());

    SegmentHelper helper = new SegmentHelper(segments);

    ArrayList<ArrayList<Double>> timbres = helper.getTimbres();
    sf.setTimbreMoments(vectorStatMoments(timbres));

    ArrayList<ArrayList<Double>> pitches = helper.getPitches();
    sf.setPitchMoments(vectorStatMoments(pitches));

    sf.setDuration(songData.getDuration());
    sf.setEnergy(songAnalysis.getEnergy());
    sf.setValence(songAnalysis.getValence());
    sf.setLoudness(songData.getLoudness());
    sf.setTempo(songData.getTempo());
    sf.setNormalisedTempo(normaliseTempo(songData.getTempo()));
    sf.setTempoConfidence(songData.getTempoConfidence());

    // Compute stats regarding tatum length
    ArrayList<Tatum> tatums = new ArrayList<>(songData.getTatums());
    TatumHelper tatumHelper = new TatumHelper(tatums);
    Statistics tatumDurationStats = new Statistics(tatumHelper.getDuration());
    sf.setTatumLengthMean(tatumDurationStats.mean());
    sf.setTatumLengthVar(tatumDurationStats.variance());

    // Compute stats regarding bars
    ArrayList<Bar> bars = new ArrayList<>(songData.getBars());
    Statistics barDurationStats = new Statistics(new BarHelper(bars).getDuration());
    sf.setBarLengthMean(barDurationStats.mean());
    sf.setBarLengthVar(barDurationStats.variance());

    ArrayList<Section> sections = new ArrayList<>(songData.getSections());
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

    ofy().delete().entities(songAnalysis, songData);
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
}
