package uk.co.yojan.kiara.analysis.resources;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.cluster.*;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.SongFeature;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Created by yojan on 12/9/14.
 */
@Path("/cluster")
public class ClusterResource {

  Logger log = Logger.getLogger(ClusterResource.class.getName());

  @GET
  @Path("/view/{playlistId}")
  public Response viewHierarchical(@PathParam("playlistId") Long playlistId) {
    NodeCluster root = ofy().load().key(Key.create(NodeCluster.class, playlistId + "-0-0")).now();

    // fetch songs to display
    ArrayList<Key<Song>> songKeys = new ArrayList<>();
    for(String songId : root.getSongIds()) {
      songKeys.add(Key.create(Song.class, songId));
    }
    Map<Key<Song>, Song> songs = ofy().load().keys(songKeys);

    if(root == null) return Response.noContent().build();

    StringBuilder html = new StringBuilder();
    Stack<Cluster> stack = new Stack<>();
    stack.push(root);

    while(!stack.isEmpty()) {
      Cluster hd = stack.pop();
      if(hd instanceof NodeCluster) {
        html.append(clusterTitle(hd));
        Collection<Cluster> children = ((NodeCluster) hd).getChildren();
        log.info(children.size() + " children found for node " + hd.getId());
        for(Cluster c : children) {
          stack.push(c);
        }
      } else if(hd instanceof LeafCluster) {
        Song leaf = songs.get(Key.create(Song.class, ((LeafCluster) hd).getSongId()));
        for(int i = 0; i < hd.getLevel(); i++) html.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        html.append(leaf.getArtist() + " - " + leaf.getSongName() + "<br>");
      }
    }

    return Response.ok().entity(html.toString()).build();
  }

  private static String clusterTitle(Cluster c) {
    int hIndex = c.getLevel() + 1;
    String[] id = c.getId().split("-");
    if(hIndex <= 6) {
      String ret = "<h" + hIndex + ">";
      for(int i = 0; i < c.getLevel(); i++) ret += "&nbsp;&nbsp;&nbsp;&nbsp;";
      ret += id[1] + "-" + id[2] + "</h" + hIndex + ">";
      return ret;
    } else
      return id[0]+"-"+id[1];
  }

  @GET
  @Path("/hierarchical/{playlistId}")
  public Response hierarch(@PathParam("playlistId") Long playlistId,
                           @DefaultValue("3") @QueryParam("k") int k) {

    return Response.ok().entity(PlaylistClusterer.cluster(playlistId, k).getId()).build();
  }

  @GET
  @Path("/kmeans/{playlistId}")
  public Response kMeans(@PathParam("playlistId") Long playlistId,
                         @DefaultValue("3") @QueryParam("k") int k) {
    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    Collection<String> songIds = p.getAllSongIds();

    Collection<Key<SongFeature>> featureKeys = new ArrayList<>();
    Collection<Key<Song>> songKeys = new ArrayList<>();
    for(String id : songIds) {
      featureKeys.add(Key.create(SongFeature.class, id));
      songKeys.add(Key.create(Song.class, id));
    }

    Collection<SongFeature> sf = ofy().load().keys(featureKeys).values();
    Map<Key<Song>, Song> songs = ofy().load().keys(songKeys);

    try {
      ArrayList<SongFeature> sfList = new ArrayList<>();
      sfList.addAll(sf);
      KMeans kmeans = new KMeans(k, sfList);
      int[] assignments = kmeans.run();

      StringBuilder sb = new StringBuilder();
      sb.append("<h1> Cluster results for " + p.getName() + "</h1><br>");
      for(int cluster = 0; cluster < k; cluster++) {
        sb.append("<h2>Cluster " + cluster + "</h2>");
        for(int i = 0; i < assignments.length; i++) {
          if(assignments[i] == cluster) {
            Song s = songs.get(Key.create(Song.class, sfList.get(i).getId()));
            sb.append(s.getArtist() + " - " + s.getSongName() + "<br>");
          }
        }
        sb.append("<br><br>");
      }
      return Response.ok().entity(sb.toString()).build();
    } catch (Exception e) {
      StringWriter writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter( writer );
      e.printStackTrace(printWriter);
      printWriter.flush();
      String stackTrace = writer.toString();
      log.warning(stackTrace);
      return Response.ok().entity(stackTrace).build();
    }
  }
}
