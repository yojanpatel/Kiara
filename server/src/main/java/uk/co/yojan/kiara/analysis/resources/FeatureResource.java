package uk.co.yojan.kiara.analysis.resources;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.cluster.agglomerative.DistanceMatrix;
import uk.co.yojan.kiara.analysis.cluster.agglomerative.DistanceMatrixBuilder;
import uk.co.yojan.kiara.analysis.cluster.agglomerative.HierarchicalClustering;
import uk.co.yojan.kiara.analysis.cluster.agglomerative.SongCluster;
import uk.co.yojan.kiara.analysis.cluster.linkage.MeanDistance;
import uk.co.yojan.kiara.analysis.tasks.FeatureExtractionTask;
import uk.co.yojan.kiara.analysis.tasks.LoadFeatures;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

@Path("/features")
public class FeatureResource {

  Logger log = Logger.getLogger("FeatureResource");

  @GET
  public Response fetchAnalysis(@QueryParam("id") String id,
                                @QueryParam("artist") String artist,
                                @QueryParam("title") String title) {
    if(id == null || id.equals("")) return Response.noContent().build();
    log.info(id);
    SongAnalysis songAnalysis = EchoNestApi.getSongAnalysis(id);
    // As a fallback, search EchoNest with the artist and title name of the song.
    // This often works for obscure or new tracks.
    if(songAnalysis == null && !artist.isEmpty() && !title.isEmpty()) {
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

  @GET
  @Path("/cluster/{playlistId}")
  public Response cluster(@PathParam("playlistId") Long playlistId) {
    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    log.info("Building distance matrix for " + playlistId);
    DistanceMatrix dm = DistanceMatrixBuilder.build(p);
    log.info("Clustering " + playlistId);
    HierarchicalClustering alg = new HierarchicalClustering(dm, new MeanDistance());
    SongCluster s = alg.run();
    Queue<SongCluster> cq = new LinkedList<>();
    cq.add(s);
    StringBuilder sb = new StringBuilder();
    while (!cq.isEmpty()) {
      SongCluster head = cq.poll();
      sb.append(head.getId()).append(head.getParent() != null ? " parent: " + head.getParent().getId() : "");
      sb.append("<br>");
      cq.addAll(head.getChildren());
    }
    return Response.ok().entity(sb.toString()).build();
  }

  @GET
  @Path("/purge")
  public Response purge() {
    List<Key<SongData>> keys = ofy().load().type(SongData.class).keys().list();
    for(Key<SongData> key : keys) {
      TaskManager.featureQueue().add(TaskOptions.Builder.withPayload(new FeatureExtractionTask(key.getName())));
    }
    return Response.ok().entity(keys.size()).build();
  }

  @GET
  @Path("/gather")
  public Response gather() {
    List<Key<Song>> keys = ofy().load().type(Song.class).keys().list();
    Map<Key<Song>, Song> songAnalysisCollection = ofy().load().keys(keys);
    List<Key<SongData>> keyssf = ofy().load().type(SongData.class).keys().list();

    int counter = 0;
    for(Song sd : songAnalysisCollection.values()) {
      if (!keyssf.contains(Key.create(SongData.class, sd.getId()))) {
        TaskManager.featureQueue().add(TaskOptions.Builder.withPayload(new LoadFeatures(sd.getSpotifyId(), sd.getArtist(), sd.getSongName())));
        counter++;
      }
    }
    return Response.ok().entity(counter).build();
  }

  @GET
  @Path("/convert")
  public Response convert() {
    List<Key<Song>> keys = ofy().load().type(Song.class).keys().list();
    List<Key<SongFeature>> keyssf = ofy().load().type(SongFeature.class).keys().list();
    int c = 0;
    for(Key<Song> k : keys) {
      if(!keyssf.contains(Key.create(SongFeature.class, k.getName()))) {
        TaskManager.featureQueue().add(TaskOptions.Builder.withPayload(new FeatureExtractionTask(k.getName())));
        c++;
      }
    }

    return Response.ok().entity(c).build();
  }

  @Path("/song/{spotifyId}")
  @GET
  public Response viewStats(@PathParam("spotifyId") String spotifyId) {
    SongFeature songFeature = ofy().load().key(Key.create(SongFeature.class, spotifyId)).now();
    Song song = ofy().load().key(Key.create(Song.class, spotifyId)).now();

    StringBuilder sb = new StringBuilder();
    sb.append("<h1><b>" + song.getArtist() + " - " + song.getSongName() + " - " + song.getAlbumName() + "</b></h1><br><br>");
    sb.append("Duration: " + songFeature.getDuration() + "<br>");
    sb.append("Loudness: " + songFeature.getLoudness() + "<br>");
    sb.append("Valence: " + songFeature.getValence() + "<br>");
    sb.append("Energy: " + songFeature.getEnergy() + "<br>");
    sb.append("Tempo: " + songFeature.getTempo() + "<br>");
    sb.append("Tempo Confidence: " + songFeature.getTempoConfidence() + "<br>");
    sb.append("<br><br>");

    sb.append("<b>Pitch Moments</b><br>");
    sb.append("<table style=\"width:100%\">\n" +
        "    <tr>\n" + //mean, variance, median, min, max, range, skewness, kurtosis
        "    <th>Mean</th>\n" +
        "    <th>Variance</th>\n" +
        "    <th>Median</th>\n" +
        "    <th>Min</th>\n" +
        "    <th>Max</th>\n" +
        "    <th>Range</th>\n" +
        "    <th>Skewness</th>\n" +
        "    <th>Kurtosis</th>\n" +
        "    </tr>");
    ArrayList<ArrayList<Double>> pitches = songFeature.getPitchMoments();
    for(ArrayList<Double> pitchVector : pitches) {
      sb.append("<tr>");
      for(Double d : pitchVector) {
        sb.append("<td>" + d + "</td>");
      }
      sb.append("</tr>");
    }
    sb.append("</table>");

    sb.append("<br>");

    sb.append("<b>Timbre Moments</b><br>");
    sb.append("<table style=\"width:100%\">\n" +
        "    <tr>\n" + //mean, variance, median, min, max, range, skewness, kurtosis
        "    <th>Mean</th>\n" +
        "    <th>Variance</th>\n" +
        "    <th>Median</th>\n" +
        "    <th>Min</th>\n" +
        "    <th>Max</th>\n" +
        "    <th>Range</th>\n" +
        "    <th>Skewness</th>\n" +
        "    <th>Kurtosis</th>\n" +
        "    </tr>");
    ArrayList<ArrayList<Double>> timbres = songFeature.getTimbreMoments();
    for(ArrayList<Double> timbreVector : timbres) {
      sb.append("<tr>");
      for(Double d : timbreVector) {
        sb.append("<td>" + d + "</td>");
      }
      sb.append("</tr>");
    }
    sb.append("</table>");

    sb.append("<br>");
    sb.append("<b>Timbre Area 2D Moments</b><br>");
    ArrayList<Double> tam = songFeature.getTimbreAreaMoments();
    if(tam != null) {
      for (int i = 0; i < tam.size(); i++) {
        sb.append(i + " " + tam.get(i) + "<br>");
      }
    }

    return Response.ok().entity(sb.toString()).build();
  }

  @GET
  @Path("/history/{playlistId}")
  public Response test(@PathParam("playlistId") Long playlistId) {
    ofy().clear();
    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    Queue<String> h = p.history();
    String t = h.size() + "  ";
    for(String s : h) {
      t += s;
    }

    return Response.ok().entity("\"" + t + "\"").build();
  }
}
