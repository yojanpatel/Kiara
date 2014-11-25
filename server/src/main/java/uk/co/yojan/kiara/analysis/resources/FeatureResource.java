package uk.co.yojan.kiara.analysis.resources;

import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.tasks.FeatureExtractionTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.SongData;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

@Path("/features")
public class FeatureResource {

  Logger log = Logger.getLogger("FeatureResource");

  @GET
  public Response fetchAnalysis(@QueryParam("id") String id,
                                @QueryParam("artist") String artist,
                                @QueryParam("title") String title) {
    log.info(id);
    SongAnalysis songAnalysis = EchoNestApi.getSongAnalysis(id);
    // As a fallback, search EchoNest with the artist and title name of the song.
    // This often works for obscure or new tracks.
    if(songAnalysis == null) {
      log.info("Failed to search with the Spotify Id, trying to search using artist name and title.");
      songAnalysis = EchoNestApi.getSongAnalysis(artist, title);
    }

    if(songAnalysis == null) {
      return Response.serverError().build();
    }

    songAnalysis.setId(id);
    SongData songData = songAnalysis.getSongData();
    songData.setSpotifyId(id);

    ofy().save().entities(songData, songAnalysis).now();
    log.info("Adding feature extraction task to featureQueue for " + id);
    TaskManager.featureQueue().add(
        TaskOptions.Builder
            .withPayload(new FeatureExtractionTask(id))
            .taskName("FeatureExtraction-" + id + "-" + System.currentTimeMillis()));

    return Response.ok().entity(id).build();
  }
}
