package uk.co.yojan.kiara.analysis.cluster;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.SongFeature;

import java.util.ArrayList;
import java.util.Collection;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Construct a distance matrix for all the songs from their playlist.
 */
public class DistanceMatrixBuilder {

  public static DistanceMatrix build(Playlist playlist) {
    DistanceMatrix matrix = new DistanceMatrix();

    ArrayList<SongFeature> features = new ArrayList<>(loadFeatures(playlist));
    ArrayList<SongCluster> baseClusters = new ArrayList<>();
    for(SongFeature f : features) {
      baseClusters.add(new SongCluster(f.getId()));
    }

    /* Currently unidirectional (for bidirectional, int j = 0; ignore if i == j) */
    for(int i = 0; i < features.size(); i++) {
      for(int j = i + 1; j < features.size(); j++) {
        matrix.insert(
            baseClusters.get(i),
            baseClusters.get(j),
            features.get(i).distanceTo(features.get(j)));
      }
    }

    return matrix;
  }

  /*
   * Load the SongFeature entities associated with playlist from the DataStore.
   */
  private static Collection<SongFeature> loadFeatures(Playlist playlist) {
    Collection<String> songIds = playlist.getAllSongIds();
    Collection<Key<SongFeature>> featureKeys = new ArrayList<>();
    for(String id : songIds) {
      featureKeys.add(Key.create(SongFeature.class, id));
    }

    return ofy().load().keys(featureKeys).values();
  }
}
