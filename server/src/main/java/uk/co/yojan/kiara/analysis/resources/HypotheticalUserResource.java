package uk.co.yojan.kiara.analysis.resources;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.cluster.FeaturesNotReadyException;
import uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer;
import uk.co.yojan.kiara.analysis.features.Statistics;
import uk.co.yojan.kiara.analysis.users.*;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Short form User IDs:
 *   MrTimbre    - T
 *   ErraticEarl - E
 *   BeatLover   - B
 */
@Path("/hypothetical")
public class HypotheticalUserResource {

  @GET
  @Path("/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("userId") String userId) {

    User u = loadHypotheticalUser(userId);
    if(u != null) {
      return Response.ok().entity(u).build();
    } else {
      return Response.noContent().entity("User ID must be one of T, E or B.").build();
    }
  }

  @POST
  @Path("/{userId}/songs")
  @Produces("application/json")
  @Consumes("application/json")
  public Response addSongs(String[] spotifyIds, @PathParam("userId") String userId) {
    User u = loadHypotheticalUser(userId);
    if(u == null) return Response.noContent().entity("userId must be t, e or b").build();
    List<Key<Song>> keys = new ArrayList<>();
    for(String s : spotifyIds) {
      keys.add(Key.create(Song.class, s.replace("\"","")));
    }
    List<Song> songs = new ArrayList<>(ofy().load().keys(keys).values());
    loadPlaylist(u).addSongs(songs).now();
    return Response.ok().build();
  }

  @GET
  @Path("/cluster")
  public Response cluster(@DefaultValue("3") @QueryParam("k") int k) throws FeaturesNotReadyException {
    Playlist t = loadPlaylist(loadHypotheticalUser("t"));
    Playlist e = loadPlaylist(loadHypotheticalUser("e"));
    Playlist b = loadPlaylist(loadHypotheticalUser("b"));
    Playlist a = loadPlaylist(loadHypotheticalUser("a"));

    PlaylistClusterer clusterer = new PlaylistClusterer();

    if(t != null)
      clusterer.cluster(t.getId(), k);

    if(e != null)
      clusterer.cluster(e.getId(), k);

    if(b != null)
      clusterer.cluster(b.getId(), k);

    if(a != null)
      clusterer.cluster(b.getId(), k);

    return Response.ok().entity("Clustering all hypothetical users...").build();
  }

  @GET
  @Path("/initialise")
  @Produces(MediaType.APPLICATION_JSON)
  public Response newHypotheticalUser() {

    User e = new ErraticEarl().user();
    User t = new MrTimbre().user();
    User b = new BeatLover().user();
    User a = new AlbumListener().user();

//    ofy().save().entities(a).now();

    User[] us = {a};
    return Response.ok().entity(us).build();
  }

  @GET
  @Path("/{userId}/{songId}")
  public Response startEpisode(@PathParam("userId") String userId,
                               @PathParam("songId") String seedSongId) {
    HypotheticalUser u;
    if(userId.toLowerCase().equals("t")) {
      u = new MrTimbre();
    } else if(userId.toLowerCase().equals("e")) {
      u = new ErraticEarl();
    } else if(userId.toLowerCase().equals("b")) {
      u = new BeatLover();
    } else if(userId.toLowerCase().equals("a")) {
      u = new AlbumListener();
    } else {
      return null;
    }

    Pair<ArrayList<Double>, ArrayList<Integer>> results = u.play(seedSongId);
    ArrayList<Double> rewards = results.getFirst();
    ArrayList<Integer> skips = results.getSecond();
    return Response.ok().entity("Ave Reward: " + new Statistics(rewards).mean() + " Skips: " + skips.size()).build();
  }

  private User loadHypotheticalUser(String userId) {
    if(userId.toLowerCase().equals("t")) {
      return ofy().load().key(Key.create(User.class, new MrTimbre().userId())).now();
    } else if(userId.toLowerCase().equals("e")) {
      return ofy().load().key(Key.create(User.class, new ErraticEarl().userId())).now();
    } else if(userId.toLowerCase().equals("b")) {
      return ofy().load().key(Key.create(User.class, new BeatLover().userId())).now();
    } else if(userId.toLowerCase().equals("a")) {
      return ofy().load().key(Key.create(User.class, new AlbumListener().userId())).now();
    } else {
      return null;
    }
  }

  private Playlist loadPlaylist(User u) {
    Collection<Playlist> playlists = u.getAllPlaylists();
    if(playlists.size() == 0) {
      // create new playlist
      Playlist p = new Playlist();
      p.setName(u.getFirstName() + " " + u.getLastName() + " Playlist");
      ofy().save().entity(p).now();
      u.addPlaylist(p);
      return p;
    } else {
      return (Playlist) playlists.toArray()[0];
    }
  }
}
