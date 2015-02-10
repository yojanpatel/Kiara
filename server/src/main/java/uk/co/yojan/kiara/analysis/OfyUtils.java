package uk.co.yojan.kiara.analysis;

import com.google.appengine.api.datastore.ReadPolicy;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.SongFeature;
import uk.co.yojan.kiara.server.models.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * A set of methods to access various entities from the datastore.
 */
public class OfyUtils {

  public static Result<User> loadUser(String userId) {
    return ofy().consistency(ReadPolicy.Consistency.STRONG).load().key(Key.create(User.class, userId));
  }

  public static Playlist loadPlaylist(String userId, Long playlistId) {
    return loadUser(userId).now().getPlaylist(playlistId);
  }


  public static Result<Song> loadSong(String songId) {
    return ofy().load().key(Key.create(Song.class, songId));
  }


  public static LoadResult<LeafCluster> loadLeafCluster(Long playlistId, String songId) {
    return ofy().load().key(Key.create(LeafCluster.class, playlistId + "-" + songId));
  }

  public static LoadResult<LeafCluster> loadLeafCluster(String leafId) {
    return ofy().load().key(Key.create(LeafCluster.class, leafId));
  }

  public static Result<NodeCluster> loadRootCluster(Long playlist) {
    return ofy().load().key(Key.create(NodeCluster.class, playlist+"-0-0"));
  }

  public static Result<NodeCluster> loadNodeCluster(String id) {
    return ofy().load().key(Key.create(NodeCluster.class, id));
  }

  public static Collection<SongFeature> loadFeatures(List<String> ids) {
    List<Key<SongFeature>> keys = new ArrayList<>();
    for(String id : ids) keys.add(Key.create(SongFeature.class, id));
    return ofy().load().keys(keys).values();
  }

  public static LoadResult<SongFeature> loadFeature(String id) {
    return ofy().load().key(Key.create(SongFeature.class, id));
  }
}
