package uk.co.yojan.kiara.server;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import uk.co.yojan.kiara.server.models.*;

import java.util.logging.Logger;

/**
 * Objectify Service - a high level abstraction for Google Datastore.
 */
public class OfyService {
  private static final Logger log = Logger.getLogger(OfyService.class.getName());

  static {
    // Register entities.
    register(User.class);
    register(Song.class);
    register(Playlist.class);
    register(SongAnalysis.class);
    register(SongData.class);
    register(SongFeature.class);
  }

  public static Objectify ofy() {
    return ObjectifyService.ofy();
  }

  public static ObjectifyFactory factory() {
    return ObjectifyService.factory();
  }

  private static void register(Class<?> clazz) {
    log.info("Registering Entity: " + clazz.getName());
    factory().register(clazz);
  }
}
