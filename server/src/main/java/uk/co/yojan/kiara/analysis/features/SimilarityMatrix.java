package uk.co.yojan.kiara.analysis.features;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.features.scaling.ZNormaliser;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.*;

import static uk.co.yojan.kiara.server.OfyService.ofy;

public class SimilarityMatrix {
  private HashMap<String, HashMap<String, Double>> similarityMatrix = new HashMap<>();
  private Playlist p;
  private Map<Key<SongFeature>, SongFeature> songFeatures;
  private List<SongFeature> songs;
  private List<Key<SongFeature>> keys;

  public SimilarityMatrix(Playlist p) {
    this.p = p;
    keys = new ArrayList<>();
    Collection<String> ids = p.getAllSongIds();
    for(String id : ids) keys.add(Key.create(SongFeature.class, id));
    songFeatures = ofy().load().keys(keys);
    songs = new ArrayList<>(songFeatures.values());
  }

  private double[][] scaleFeatures() throws IllegalAccessException {
    double[][] unscaled = new double[keys.size()][SongFeature.getFeatureNames().size()];
    for(int i = 0; i < songs.size(); i++) {
      unscaled[i] = songs.get(i).getFeatureValues();
    }

    return new ZNormaliser().scale(unscaled);
  }

  private void construct(Playlist p) throws IllegalAccessException {
    double[][] scaled = scaleFeatures();

    // initialise
    for(int i = 0; i < songFeatures.size(); i++) {
      similarityMatrix.put(keys.get(i).getName(), new HashMap<String, Double>());
    }

    // populate
    for(int i = 0; i < songFeatures.size(); i++) {
      for(int j = i + 1; j < songFeatures.size(); j++) {
        SongFeature a = songFeatures.get(keys.get(i));
        SongFeature b = songFeatures.get(keys.get(j));
        double distance = euclidean(scaled[i], scaled[j]);
        similarityMatrix.get(a.getId()).put(b.getId(), distance);
        similarityMatrix.get(b.getId()).put(a.getId(), distance);
      }
    }
  }

  public HashMap<String, HashMap<String, Double>> get() throws IllegalAccessException {
    if(similarityMatrix.isEmpty()) {
      construct(p);
    }
    return similarityMatrix;
  }

  private double euclidean(double[] a, double[] b) {
    double curr = 0.0;
    for(int i = 0; i < a.length; i++) {
      if(!Double.isNaN(a[i]) && !Double.isNaN(b[i]))
        curr += Math.pow(a[i] - b[i], 2);
    }
    return Math.sqrt(curr);
  }

  public Double get(String idA, String idB) throws IllegalAccessException {
    if(similarityMatrix.isEmpty()) {
      construct(p);
    }
    Double ret =  similarityMatrix.get(idA).get(idB);
    if(ret == null) {
      ret =  similarityMatrix.get(idB).get(idA);
    }
    return ret;
  }
}
