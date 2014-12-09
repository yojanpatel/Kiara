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

  /*
   * GET request handler for all playlists for a given user.
   *
   * @param userId  the user id (Spotify id) to get the playlists for
   * @param detail  true if extra data about the playlist's songs is also to be sent
   * @param request  the request object that triggered the handler
   * @return if E-tag is valid, a 304 Not Modified otherwise the updated resource
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAll(@PathParam("user_id") String userId,
                         @DefaultValue("false") @QueryParam("detail") boolean detail,
                         @Context Request request) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();

    EntityTag etag = new EntityTag(u.v());
    Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
    CacheControl cc = new CacheControl();

    if(builder != null) {
      Logger.getLogger("PlaylistResource").info("Cached resource did change " + etag.getValue());
      return builder.build();
    }

    // cached resource did change, serve updated content.
    if(detail) {
      builder = Response.ok().entity(u.getPlaylistsWithSongs()).tag(etag);
    } else {
      builder = Response.ok().entity(u.getAllPlaylists()).tag(etag);
    }
    return builder.cacheControl(cc).build();
  }

  /*
   * GET request handler for a specific playlist for the user.
   *
   * Fetches the user, followed by the playlist and if the detail param is
   * set to true, the songs from the Datastore.
   *
   * @param userId  the user id (Spotify id) to get the playlist for
   * @param id  the playlist identifier (long)
   * @param detail  whether to also send the playlist's song data.
   * @return if E-tag is valid, 304 Not Modified otherwise the updated resource.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public Response get(@PathParam("user_id") String userId,
                      @PathParam("id") Long id,
                      @DefaultValue("false") @QueryParam("detail") boolean detail,
                      @Context Request request) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    Playlist playlist = u.getPlaylist(id);
    if(playlist == null) {
      return Response.noContent().build();
    }

    EntityTag etag = new EntityTag(playlist.v());
    Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
    CacheControl cc = new CacheControl();

    if(builder != null) {
      Logger.getLogger("PlaylistResource").info("Cached playlist resource did not change " + etag.getValue());
      return builder.build();
    }

    if(detail) {
      ArrayList<Song> songs = new ArrayList<Song>(playlist.getAllSongs());
      builder = Response.ok().entity(new PlaylistWithSongs(playlist, songs));
    } else {
      builder =  Response.ok().entity(playlist);
    }

    return builder.cacheControl(cc).build();
  }

  /*
   * DELETE the playlist resource associated with the param id.
   *
   * @param userId  the user id whose playlist to delete.
   * @param id  the playlist identifier to delete.
   * @return Not Modified if playlist was not deleted, else 200 OK.
   */
  @DELETE
  @Path("/{id}")
  public Response delete(@PathParam("user_id") String userId, @PathParam("id") Long id) {
    User owner = ofy().load().key(Key.create(User.class, userId)).now();

    if(owner.removePlaylist(id)) {
      ofy().delete().key(Key.create(Playlist.class, id)); // async
      return Response.ok().build();
    } else {
      return Response.notModified().build();
    }
  }

  /*
   * PUT (not used yet) handler to update the resource.
   */
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

  /*
   * POST handler to create a new playlist resource.
   *
   * @param userId  the user id to who the playlist belongs.
   * @param uri  the uri from the request context.
   * @param item  the JSON playlist resource to create.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(@PathParam("user_id") String userId,
                         @Context UriInfo uri,
                         Playlist item,
                         @Context Request request) {
    Result<User> ownerResult = ofy().load().key(Key.create(User.class, userId)); // async
    item.updateLastViewed();
    ofy().save().entity(item).now(); // sync

    User owner = ownerResult.now(); // wait
    owner.addPlaylist(item);
    URI plUri = UriBuilder.fromUri(uri.getRequestUri()).path(item.getId().toString()).build();
    return Response.created(plUri).entity(item).build();
  }
}
