package uk.co.yojan.kiara.server.resources;

import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
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
  public Collection<Song> getSongs(@PathParam("user_id") String userId,
                                   @PathParam("playlist_id") Long playlistId) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    return u.getPlaylist(playlistId).getAllSongs();
  }

  @POST
  public Response addSong(String spotifyId,
                          @PathParam("user_id") String userId,
                          @PathParam("playlist_id") Long playlistId) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();

    if(!u.hasPlaylist(playlistId)) {
      return Response.notModified().build();
    }

    Song created;
    if(!u.hasSong(spotifyId)) {
      log.info("User does not have song. Adding.");
      try {
        created = u.addSong(spotifyId);
      } catch(Exception e) {
        e.printStackTrace();
        return Response.serverError().build();
      }
    } else {
      log.info("User already has song. Loading.");
      created = u.getSongFromSpotifyId(spotifyId);
    }

    u.getPlaylist(playlistId).addSong(u.getIdFromSpotifyId(spotifyId));
    return Response.ok().entity(created).build();
  }
}
