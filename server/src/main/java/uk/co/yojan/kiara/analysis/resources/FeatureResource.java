package uk.co.yojan.kiara.analysis.resources;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.cluster.*;
import uk.co.yojan.kiara.analysis.cluster.linkage.MeanDistance;
import uk.co.yojan.kiara.analysis.tasks.FeatureExtractionTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.*;

import javax.ws.rs.*;
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
    Map<Key<SongData>, SongData> songAnalysisCollection = ofy().load().keys(keys);

    for(SongData sd : songAnalysisCollection.values()) {
      TaskManager.featureQueue().add(TaskOptions.Builder.withPayload(new FeatureExtractionTask(sd.getSpotifyId())));
    }
    return Response.ok().entity(songAnalysisCollection.size()).build();
  }

  @GET
  @Path("/gather")
  public Response gather() {
    List<Key<Song>> keys = ofy().load().type(Song.class).keys().list();
    Map<Key<Song>, Song> songAnalysisCollection = ofy().load().keys(keys);

    List<Key<SongFeature>> keys2 = ofy().load().type(SongFeature.class).keys().list();
    Map<Key<SongFeature>, SongFeature> songFeatureCollection = ofy().load().keys(keys2);

    HashSet<String> ids = new HashSet<>();
    for(SongFeature sf : songFeatureCollection.values()) {
      ids.add(sf.getId());
    }


    int counter = 0;
    for(Song sd : songAnalysisCollection.values()) {
      if(!ids.contains(sd.getSpotifyId())) {
        TaskManager.fetchAnalysis(sd.getSpotifyId(), sd.getArtist(), sd.getSongName());
        counter++;
      }
    }
    return Response.ok().entity(counter).build();
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
    for(ArrayList<Double> timbreVector : pitches) {
      sb.append("<tr>");
      for(Double d : timbreVector) {
        sb.append("<td>" + d + "</td>");
      }
      sb.append("</tr>");
    }
    sb.append("</table>");
    return Response.ok().entity(sb.toString()).build();
  }

  @GET
  @Path("/test")
  public Response test() {
    return Response.ok().entity("Testing..").build();
  }
}
