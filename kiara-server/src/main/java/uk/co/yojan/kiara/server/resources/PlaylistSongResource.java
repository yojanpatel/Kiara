package uk.co.yojan.kiara.server.resources;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Collection;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Playlist Song Resource
 *
 * actions related to songs with respect to a user and a playlist.
 */
@Path("users/{user_id}/playlists/{playlist_id}/songs")
@Produces("application/json")
public class PlaylistSongResource {
  private static Logger log = Logger.getLogger(PlaylistSongResource.class.getName());

  @GET
  public Response getSongs(@PathParam("user_id") String userId,
                                   @PathParam("playlist_id") Long playlistId,
                                   @Context Request request) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    Playlist p = u.getPlaylist(playlistId);
    EntityTag etag = new EntityTag(Long.toString(p.v()));
    Response.ResponseBuilder builder = request.evaluatePreconditions(etag);
    CacheControl cc = new CacheControl();

    // cached resource did change, serve updated content.
    if(builder == null) {
      builder = Response.ok().entity(p.getAllSongs()).tag(etag);
    }
    return builder.cacheControl(cc).build();
  }

  @GET
  @Path("/try")
  public Response get() {
    String spotifyId = "5dHpbFmZjWucrol0M7aNGU";
    Song created = null;
    try {
      created = Song.newInstanceFromSpotify(spotifyId);
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().entity(e.getMessage()).build();
    }
    return Response.ok().entity(created).build();
  }

  @POST
  public Response addSong(String spotifyId,
                          @PathParam("user_id") String userId,
                          @PathParam("playlist_id") Long playlistId) {

    User u = ofy().load().key(Key.create(User.class, userId)).now();
    Result us = ofy().save().entity(u);

    if(!u.hasPlaylist(playlistId)) {
      return Response.notModified().build();
    }

    spotifyId = spotifyId.replace("\"", "");

    Song created;
    Result save = null;
    if(!Song.exists(spotifyId)) {
      try {
        created = Song.newInstanceFromSpotify(spotifyId);
      } catch (Exception e) {
        e.printStackTrace();
        return Response.serverError().entity(e.getMessage()).build();
      }
      save = ofy().save().entity(created);
    } else {
      created = ofy().load().key(Key.create(Song.class, spotifyId)).now();
    }

    assert created.getSpotifyId().equals(spotifyId);

    Playlist p = u.getPlaylist(playlistId);
    p.addSong(spotifyId);

    us.now();
    if(save != null)
      save.now();

    return Response.ok().entity(created).build();
  }
}
