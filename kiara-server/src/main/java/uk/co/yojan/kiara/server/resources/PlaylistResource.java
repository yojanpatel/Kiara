package uk.co.yojan.kiara.server.resources;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.PlaylistWithSongs;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Playlist Resource.
 *
 * Represents a playlist, consisting of Songs to be played on the clients.
 * Inherits @Path("user/{user_id}/playlist") from PlaylistSongBaseResource.
 */
@Path("users/{user_id}/playlists")
public class PlaylistResource {
  private static Logger log = Logger.getLogger(Playlist.class.getName());

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(@PathParam("user_id") String userId,
                         @DefaultValue("false") @QueryParam("detail") boolean detail,
                         @Context Request request) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    EntityTag etag = new EntityTag(Long.toString(u.v()));
    Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
    CacheControl cc = new CacheControl();
//    cc.setMaxAge(60);
    if(builder != null) {
      Logger.getLogger("PlaylistResource").info("Cached resource did change " + etag.getValue());
      return builder.build();
    }
    // cached resource did change, serve updated content.
//    if(builder == null) {
      if(detail) {
        builder = Response.ok().entity(u.getPlaylistsWithSongs()).tag(etag);
      } else {
        builder = Response.ok().entity(u.getAllPlaylists()).tag(etag);
      }
//    }
    return builder.cacheControl(cc).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Response get(@PathParam("user_id") String userId,
                      @PathParam("id") Long id,
                      @DefaultValue("false") @QueryParam("detail") boolean detail) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    if(detail) {
      Playlist playlist = u.getPlaylist(id);
      ArrayList<Song> songs = new ArrayList<Song>(playlist.getAllSongs());
      return Response.ok().entity(new PlaylistWithSongs(playlist, songs)).build();
    }

    Playlist p = u.getPlaylist(id);
    return Response.ok().entity(p).build();
  }

  @DELETE
  @Path("/{id}")
  public Response delete(@PathParam("user_id") String userId, @PathParam("id") Long id) {
    User owner = ofy().load().key(Key.create(User.class, userId)).now();
    owner.removePlaylist(id);
    ofy().delete().key(Key.create(Playlist.class, id)); // async
    return Response.ok().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Response update(@PathParam("id") Long id, Playlist item) {
    Playlist loaded = ofy().load().key(Key.create(Playlist.class, id)).now();
    if(loaded == null) {
      return Response.noContent().build();
    } else {
      loaded.copyFrom(item);
      loaded.incrementCounter();
      ofy().save().entity(loaded); // async
      return Response.ok().entity(loaded).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(@PathParam("user_id") String userId,
                         @Context UriInfo uri,
                         Playlist item,
                         @Context Request request) {
//    return Response.ok().build();
    Result<User> ownerResult = ofy().load().key(Key.create(User.class, userId)); // async
    item.updateLastViewed();
    ofy().save().entity(item).now(); // sync

    User owner = ownerResult.now(); // wait
    owner.addPlaylist(item);
    URI plUri = UriBuilder.fromUri(uri.getRequestUri()).path(item.getId().toString()).build();
    return Response.created(plUri).entity(item).build();
  }
}
