package uk.co.yojan.kiara.server.resources;

import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.tasks.BatchAddSongTask;
import uk.co.yojan.kiara.analysis.tasks.ReClusterTask;
import uk.co.yojan.kiara.analysis.tasks.RemoveSongTask;
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

  @POST
  public Response addSong(String spotifyId,
                          @PathParam("user_id") String userId,
                          @PathParam("playlist_id") Long playlistId) {

    User u = ofy().load().key(Key.create(User.class, userId)).now();
    u.incrementCounter();
    ofy().save().entity(u);

    if(!u.hasPlaylist(playlistId)) {
      return Response.notModified().build();
    }

    spotifyId = spotifyId.replace("\"", "").replace("spotify:track:", "");
    Playlist p = u.getPlaylist(playlistId);
    String[] singleton = {spotifyId};

    Song created;
    if(!Song.exists(spotifyId)) {
      try {
        created = Song.newInstanceFromSpotify(spotifyId);
        p.addSong(spotifyId).now();

        // Add a new task to the TaskQueue fetch analysis from EchoNest and persist.
        // don't fetch analysis if the user is control since there is no point
        if(u.isTest()) {
          TaskManager.fetchAnalysis(spotifyId, created.getArtist(), created.getSongName());
          updateClusters(p, singleton, 15 * 1000);
        }
      } catch (Exception e) {
        e.printStackTrace();
        return Response.serverError().entity(e.getMessage()).build();
      }
      ofy().save().entity(created);
    } else {
      // song already exists in the database.
      p.addSong(spotifyId).now();
      created = ofy().load().key(Key.create(Song.class, spotifyId)).now();
      if(u.isTest()) {
        updateClusters(p, singleton, 0);
      }
    }

    assert created.getSpotifyId().equals(spotifyId);


    return Response.ok().entity(created).build();
  }

  public void updateClusters(Playlist p, String[] ids, long delay) {
    Long playlistId = p.getId();
    if (p.needToRecluster()) {
      log.warning("Playlist has to be reclustered. Task added to the cluster queue.");
      TaskManager.clusterQueue().add(TaskOptions.Builder
          .withPayload(new ReClusterTask(playlistId))
//          .countdownMillis(delay)
          .retryOptions(RetryOptions.Builder.withTaskRetryLimit(4).minBackoffSeconds(30))
          .taskName("ReCluster-" + playlistId + "-" + System.currentTimeMillis()));
    } else {
      log.warning("Adding based on a greedy approach.");
      // ad-hoc: update the playlist cluster representation
      TaskManager.clusterQueue().add(TaskOptions.Builder
          .withPayload(new BatchAddSongTask(ids, playlistId))
//          .countdownMillis(delay)
          .retryOptions(RetryOptions.Builder.withTaskRetryLimit(4).minBackoffSeconds(30))
          .taskName("AddSong-" + playlistId + "-" + System.currentTimeMillis()));
    }
  }

  @POST
  @Path("/batch")
  public Response batchAddSongs(String[] spotifyIds,
                                @PathParam("user_id") String userId,
                                @PathParam("playlist_id") Long playlistId) {
    // Format the spotify ids so they consist only of the string id for the tracks.
    formatSpotifyTrackIds(spotifyIds);
//
//    // Load the user and playlist from the datastore.
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    u.incrementCounter();
    ofy().save().entity(u);

    if(!u.hasPlaylist(playlistId)) {
      return Response.notModified().build();
    }
    Playlist p = u.getPlaylist(playlistId);

    ArrayList<Result> songSaveResults = new ArrayList<>();
    ArrayList<String> errorTracks = new ArrayList<>();
    ArrayList<Song> createdSongs = new ArrayList<>();

    boolean fetchFlag = false;

    // Either download meta-data from Spotify and add to the datastore, or load from datastore
    // if it already exists.
    for(String trackId : spotifyIds) {
      Song created = null;

      /*
       * Check if the song already exists in the Datastore.
       * If it does not, create a new Song entity using meta-data from Spotify and persist.
       * Else, load the existing entity from the Datatore.
       */
      if(!Song.exists(trackId)) {
        try {
          created = Song.newInstanceFromSpotify(trackId);
          // Add a new task to the TaskQueue fetch analysis from EchoNest and persist.
          if(u.isTest()) {
            TaskManager.fetchAnalysis(trackId, created.getArtist(), created.getSongName());
            fetchFlag = true;
          }
          log.info(created.getSongName() + " did not exist. adding.");
        } catch (Exception e) {
          log.warning(e.getMessage());
          errorTracks.add(trackId);
        }
        if(created != null)
          songSaveResults.add(ofy().save().entity(created));

      // i.e. Song exists in the database, can just load it.
      } else {
        created = ofy().load().key(Key.create(Song.class, trackId)).now();
        log.info(created.getSongName() + " did exist. loading.");
      }

      if(created != null) {
        assert created.getSpotifyId().equals(trackId);
        createdSongs.add(created);
      }
    }

    // Update the HashMap for playlist.
    p.addSongs(createdSongs).now();

    if(u.isTest()) {
      if (fetchFlag) {
        log.warning("There were some songs with no features, will fetch them and try updating the clusters in 15 seconds.");
        updateClusters(p, spotifyIds, 15 * 1000);
      } else {
        log.warning("All songs had features, will try to update the clusters now.");
        updateClusters(p, spotifyIds, 0);
      }
    }

    return Response.ok().entity(createdSongs).build();
  }

  @DELETE
  @Path("/{song_id}")
  public Response deleteSong(@PathParam("song_id") String spotifyId,
                             @PathParam("user_id") String userId,
                             @PathParam("playlist_id") Long playlistId) {
    User u = ofy().load().key(Key.create(User.class, userId)).now();
    u.incrementCounter();

    if(!u.hasPlaylist(playlistId)) {
      return Response.notModified().build();
    }

    spotifyId = spotifyId.replace("\"", "").replace("spotify:track:", "");
    Playlist p = u.getPlaylist(playlistId);
    p.removeSong(spotifyId).now();
    TaskManager.getQueue().add(TaskOptions.Builder
        .withPayload(new RemoveSongTask(playlistId, spotifyId))
        .taskName("DeleteSong-" + playlistId + "-" + System.currentTimeMillis()));

    return Response.ok().build();
  }


  private void formatSpotifyTrackIds(String[] ids) {
    for(int i = 0; i < ids.length; i++) {
      ids[i] =  ids[i].replace("\"", "").replace("spotify:track:", "");
    }
  }
}
