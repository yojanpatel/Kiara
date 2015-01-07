package uk.co.yojan.kiara.analysis.resources;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.users.BeatLover;
import uk.co.yojan.kiara.analysis.users.ErraticEarl;
import uk.co.yojan.kiara.analysis.users.MrTimbre;
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
//    String[] spotifyIds = ids.split(",");
    List<Key<Song>> keys = new ArrayList<>();
    for(String s : spotifyIds) {
      keys.add(Key.create(Song.class, s.replace("\"","")));
    }
    List<Song> songs = new ArrayList<>(ofy().load().keys(keys).values());
//    Logger.getLogger("").warning(songs.size()+" " + spotifyIds[0]);
    loadPlaylist(u).addSongs(songs).now();
    return Response.ok().build();
  }

  @GET
  @Path("/initialise")
  @Produces(MediaType.APPLICATION_JSON)
  public Response newHypotheticalUser() {

    User e = new ErraticEarl().user();
    User t = new MrTimbre().user();
    User b = new BeatLover().user();

    ofy().save().entities(t, e, b).now();

    User[] us = {t, e, b};
    return Response.ok().entity(us).build();
  }

  private User loadHypotheticalUser(String userId) {
    if(userId.toLowerCase().equals("t")) {
      return ofy().load().key(Key.create(User.class, new MrTimbre().userId())).now();
    } else if(userId.toLowerCase().equals("e")) {
      return ofy().load().key(Key.create(User.class, new ErraticEarl().userId())).now();
    } else if(userId.toLowerCase().equals("b")) {
      return ofy().load().key(Key.create(User.class, new BeatLover().userId())).now();
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
      return p;
    } else {
      return (Playlist) playlists.toArray()[0];
    }
  }
}
