package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.features.SegmentHelper;
import uk.co.yojan.kiara.analysis.features.Statistics;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.echonest.data.Segment;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.SongData;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.lang.reflect.Array;
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
    sf.setTempoConfidence(songData.getTempoConfidence());

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
}
