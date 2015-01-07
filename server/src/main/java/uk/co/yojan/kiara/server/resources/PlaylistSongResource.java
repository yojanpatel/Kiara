package uk.co.yojan.kiara.server.resources;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.tasks.AddSongTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;
import uk.co.yojan.kiara.server.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
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
    if(p == null) return Response.noContent().build();
    EntityTag etag = new EntityTag(p.v());
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
      log.info(e.toString());
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
    u.incrementCounter();
    Result us = ofy().save().entity(u);

    if(!u.hasPlaylist(playlistId)) {
      return Response.notModified().build();
    }

    spotifyId = spotifyId.replace("\"", "").replace("spotify:track:", "");

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
    p.addSong(spotifyId).now();

    us.now();
    if(save != null)
      save.now();

    // update the playlist cluster representation
    TaskManager.featureQueue().add(TaskOptions.Builder
        .withPayload(new AddSongTask(spotifyId, playlistId))
        .taskName("AddSong-" + spotifyId + "-" + playlistId + "-" +System.currentTimeMillis()));

    return Response.ok().entity(created).build();
  }

  @POST
  @Path("/batch")
  public Response batchAddSongs(String[] spotifyIds,
                                @PathParam("user_id") String userId,
                                @PathParam("playlist_id") Long playlistId) {
    // Format the spotify ids so they consist only of the string id for the tracks.
    formatSpotifyTrackIds(spotifyIds);

    // Load the user and playlist from the datastore.
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    u.incrementCounter();
    Result us = ofy().save().entity(u);

    if(!u.hasPlaylist(playlistId)) {
      return Response.notModified().build();
    }
    Playlist p = u.getPlaylist(playlistId);


    ArrayList<Result> songSaveResults = new ArrayList<>();
    ArrayList<String> errorTracks = new ArrayList<>();
    ArrayList<Song> createdSongs = new ArrayList<>();

    // Either download meta-data from Spotify and add to the datastore, or load from datastore
    // if it already exists.
    for(String trackId : spotifyIds) {
      Song created = null;
      Result save = null;

      /*
       * Check if the song already exists in the Datastore.
       * If it does not, create a new Song entity using meta-data from Spotify and persist.
       * Else, load the existing entity from the Datatore.
       */
      if(!Song.exists(trackId)) {
        try {
          created = Song.newInstanceFromSpotify(trackId);
          log.info(created.getSongName() + " did not exist. adding.");
        } catch (Exception e) {
          log.warning(e.getMessage());
          errorTracks.add(trackId);
        }
        if(created != null)
          songSaveResults.add(ofy().save().entity(created));
      } else {
        created = ofy().load().key(Key.create(Song.class, trackId)).now();
        log.info(created.getSongName() + " did exist. loading.");
      }

      if(created != null) {
        assert created.getSpotifyId().equals(trackId);
        createdSongs.add(created);
      }
    }

    us.now();
    p.addSongs(createdSongs).now();
    for(Result r : songSaveResults) r.now();
    // Update the HashMap for playlist.

    return Response.ok().entity(createdSongs).build();
  }


  private void formatSpotifyTrackIds(String[] ids) {
    for(int i = 0; i < ids.length; i++) {
      ids[i] =  ids[i].replace("\"", "").replace("spotify:track:", "");
    }
  }
}
