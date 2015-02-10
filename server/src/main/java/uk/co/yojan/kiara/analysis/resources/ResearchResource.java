package uk.co.yojan.kiara.analysis.resources;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.wrapper.spotify.exceptions.WebApiException;
import uk.co.yojan.kiara.analysis.features.Statistics;
import uk.co.yojan.kiara.analysis.research.ARFFCreator;
import uk.co.yojan.kiara.analysis.research.Experiment;
import uk.co.yojan.kiara.analysis.research.ExperimentRunner;
import uk.co.yojan.kiara.analysis.tasks.ExperimentTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.analysis.users.*;
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
  @Path("/{userId}/{expId}")
  public Response startExperiment(@PathParam("userId") String userId,
                                  @PathParam("expId") String expId) {
    HypotheticalUser u;
    Experiment experiment = ofy().load().key(Key.create(Experiment.class, expId)).now();

    if(userId.toLowerCase().equals("t")) {
      u = new MrTimbre(experiment.playlist());
    } else if(userId.toLowerCase().equals("e")) {
      u = new ErraticEarl();
    } else if(userId.toLowerCase().equals("b")) {
      u = new BeatLover();
    } else if(userId.toLowerCase().equals("a")) {
      u = new AlbumListener();
    } else {
      return null;
    }
    ExperimentRunner.run(u, experiment);
    return Response.ok().entity("Done with experiment.").build();
  }

  @GET
  @Path("results/{expId}")
  @Produces(MediaType.TEXT_HTML)
  public Response viewResultsHyp(@PathParam("expId") String id,
                                 @DefaultValue("") @QueryParam("q") String query,
                                 @DefaultValue("-100") @QueryParam("r") int rewardThres) {
    Experiment exp = ofy().load().key(Key.create(Experiment.class, id)).now();
    HashMap<String, ArrayList<Integer>> results = exp.getSkips();
    HashMap<String, ArrayList<Double>> rewardsMap = exp.getRewardMap();
    HashMap<String, Double> rewards = exp.getRewards();

    String output = "";
    ArrayList<String> rs = new ArrayList<>(results.keySet());

    Collections.sort(rs);
    for(String label : rs) {
      Double reward = rewards.get(label);
      String rewardUrl = null;
      if(reward == null) {
        ArrayList<Double> rews = rewardsMap.get(label);
        reward = new Statistics(rews).mean();
        rewardUrl = constructRewardChart(rews);
      }

      if(label.toLowerCase().contains(query.toLowerCase()) && reward > rewardThres) {
        if(label.contains("Run0")) output += "<br><hr><br>";
        output += "<h4>" + label + "</h4>";
        ArrayList<Integer> skips = results.get(label);
        output += "Skips: " + skips.size() + " Reward: " + reward + "<br>";
        output += "<img src=\"" + constructSkipChart(skips, 5, label) + "\"><br>";
        if(rewardUrl != null) output += "<img src=\"" + rewardUrl + "\"><br>";
        output += "Number of skips for every 5 plays <br>";
      }
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

  @POST
  @Path("/{id}")
  @Consumes("application/json")
  @Produces("applcation/json")
  public Response createExperiment(@PathParam("id") String id, String[] ids){
    Experiment e = new Experiment(id);
    ofy().save().entities(e).now();
    e.addPlaylist(ids);
    String[] a = {"A", "B"};
    return Response.ok().entity(a).build();
  }

  @POST
  @Path("/download")
  @Consumes("application/json")
  @Produces("applcation/json")
  public Response download(String[] ids) throws Exception {
    for(String id : ids) {
      Song sm = Song.newInstanceFromSpotify(id);
      ofy().save().entity(sm);
      // Add a new task to the TaskQueue fetch analysis from EchoNest and persist.
      TaskManager.fetchAnalysis(id, sm.getArtist(), sm.getSongName());
    }
    return Response.ok().entity("downloading.").build();
  }


  private String constructSkipChart(List<Integer> skipData, int windowSize, String label) {
    StringBuilder url = new StringBuilder();
    url.append("http://chart.googleapis.com/chart?chxt=x,y&chxt=x,y&chxr=0,0,100|1,0,5&chs=800x200&cht=s&chd=t:");
    // <xvals>|<yvals>12,87,75,41,23,96,68,71,34,9|98,60,27,34,56,79,58,74,18,76

    int xNum = 500 / windowSize;
    if(label.contains("1000")) xNum = 1000/windowSize;
    StringBuilder xd = new StringBuilder();
    StringBuilder yd = new StringBuilder();

    HashMap<Integer, Integer> data = new HashMap<>();
    for(int i = 0; i < xNum; i++) data.put(i, 0);
    for(int skipPos : skipData) {
      data.put(skipPos / windowSize, data.get(skipPos / windowSize) + 1);
    }

    for(Integer key : data.keySet()) {
      xd.append(key).append(",");
      yd.append(data.get(key) * 20).append(",");
    }

    return url.append(xd.substring(0, xd.length() - 1)).append("|").append(yd.substring(0, yd.length() - 1)).toString();
  }

  private String constructRewardChart(List<Double> rewardData) {
    StringBuilder url = new StringBuilder();
    url.append("http://chart.googleapis.com/chart?&chs=800x200&cht=s&chd=t:");
    // <xvals>|<yvals>12,87,75,41,23,96,68,71,34,9|98,60,27,34,56,79,58,74,18,76
    StringBuilder xd = new StringBuilder();
    StringBuilder yd = new StringBuilder();

    int window_size = 2;
    int xNum = rewardData.size() / window_size;
    for(int i = 0; i < xNum; i += window_size) {
      double sum = 0.0;
      for(int j = 0; j < window_size; j++ ) {
        sum += rewardData.get(i + j);
      }
      xd.append(i).append(",");
      yd.append(50 + 50 * (sum / window_size)).append(",");
    }
    return url.append(xd.substring(0, xd.length() - 1)).append("|").append(yd.substring(0, yd.length() - 1)).toString();
  }
}
