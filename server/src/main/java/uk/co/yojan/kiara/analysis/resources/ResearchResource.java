package uk.co.yojan.kiara.analysis.resources;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.wrapper.spotify.exceptions.WebApiException;
import uk.co.yojan.kiara.analysis.research.ARFFCreator;
import uk.co.yojan.kiara.analysis.research.Experiment;
import uk.co.yojan.kiara.analysis.tasks.ExperimentTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.SongFeature;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Created by yojan on 12/28/14.
 */
@Path("/research")
public class ResearchResource {

  /**
   *
   * @param uris is a string consisting of spotify playlist ids, delimited by ';'
   */
  @GET
  public Response startExperiment(@QueryParam("p") String uris,
                                  @DefaultValue("3") @QueryParam("k") int k) throws IOException, WebApiException {
    String[] playlistURIs = uris.split(";");
    Logger.getLogger("dsd").warning(playlistURIs[1]);
    TaskManager.clusterQueue().add(TaskOptions.Builder
        .withPayload(new ExperimentTask(uris, k))
        .taskName("Experiment-Cluster-Weights-" + System.currentTimeMillis()));
    return Response.ok().build();
  }

  @GET
  @Path("/{expId}")
  public Response viewResults(@PathParam("expId") String id) {
    Experiment exp = ofy().load().key(Key.create(Experiment.class, id)).now();
    HashMap<ArrayList<Double>, Double> scoreMap = exp.getScoreMap();

    String output = "";

    for(ArrayList<Double> weights : scoreMap.keySet()) {
      for(Double w : weights) {
        output += w + " ";
      }
      output += "<br>" + scoreMap.get(weights) + "<br><br>";
    }
    return Response.ok(output).build();
  }


  @GET
  @Path("/arff")
  public Response arffCreator() throws FileNotFoundException, UnsupportedEncodingException, IllegalAccessException {
    return Response.ok().entity(ARFFCreator.constructInstances()).build();
  }

  @GET
  @Path("/view/{expId}")
  public Response kMeans(@PathParam("expId") String experimentId) {
    Experiment exp = ofy().load().key(Key.create(Experiment.class, experimentId)).now();
    LinkedHashMap<String, Integer> playlistMap = exp.getPlaylistMap();
    HashMap<ArrayList<Double>, int[]> resultsMap = exp.getResultsMap();
    ArrayList<String> songIds = new ArrayList<>(playlistMap.keySet());
    HashMap<ArrayList<Double>, Double> scoreMap = exp.getScoreMap();

    Collection<Key<Song>> songKeys = new ArrayList<>();
    for(String id : songIds) {
      songKeys.add(Key.create(Song.class, id));
    }

    Map<Key<Song>, Song> songs = ofy().load().keys(songKeys);
    String output = "";
    for(ArrayList<Double> weights : resultsMap.keySet()) {
      try {
        int[] assignments = resultsMap.get(weights);

        StringBuilder sb = new StringBuilder();
        for(Double d : weights)
          sb.append(d + " ");
        sb.append("<br>");
        sb.append(scoreMap.get(weights) + "<br>");

        for (int cluster = 0; cluster < exp.getK(); cluster++) {
          sb.append("<h2>Cluster " + cluster + "</h2>");
          for (int i = 0; i < assignments.length; i++) {
            if (assignments[i] == cluster) {
              Song s = songs.get(Key.create(Song.class, songIds.get(i)));
              sb.append(s.getArtist() + " - " + s.getSongName() + " (" + playlistMap.get(s.getId()) + ") <br>");
            }
          }
          sb.append("<br><br>");
        }
        output += sb.toString() + "<br><br><br>";
      } catch (Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();
        String stackTrace = writer.toString();
        return Response.ok().entity(stackTrace).build();
      }
    }
    return Response.ok().entity(output).build();
  }


  @GET
  @Path("/counts")
  @Produces(MediaType.TEXT_PLAIN)
  public Response count(@DefaultValue("0") @QueryParam("s") int stat) {
    List<Key<SongFeature>> keys = ofy().load().type(SongFeature.class).keys().list();
    Collection<SongFeature> songs = ofy().load().keys(keys).values();
    String[] statTitles = {"mean", "variance", "median", "min", "max", "range", "skewness", "kurtosis"};

    StringBuilder sb = new StringBuilder();
    sb.append("Counts for " + songs.size() + " songs.\n");
    sb.append(statTitles[stat] + "\n");

    for(int i = 0; i < 12; i++) {
      sb.append("Timbre Vector " + i);
      Double max = Double.NEGATIVE_INFINITY;
      Double min = Double.POSITIVE_INFINITY;
      Double sum = 0.0;

      for(SongFeature sf : songs) {
        ArrayList<Double> timbre = sf.getTimbreMoment(i);
        Double val = timbre.get(stat);

        // update max, min, average
        max = Math.max(max, val);
        min = Math.min(min, val);
        sum += val;
      }

      double mean = sum/songs.size();
      double sumSq = 0;
      for(SongFeature sf : songs) {
        ArrayList<Double> timbre = sf.getTimbreMoment(i);
        Double val = timbre.get(stat);
        sumSq += Math.pow(val - mean, 2);
      }

      double std = Math.sqrt(sumSq/songs.size());

      sb.append(" Min: " + min + ", Max: " + max + ", Mean: " + mean + ", StdDev: " + std + "\n");
    }
    return Response.ok().entity(sb.toString()).build();
  }
}
