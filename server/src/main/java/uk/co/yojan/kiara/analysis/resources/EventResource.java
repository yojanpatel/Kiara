package uk.co.yojan.kiara.analysis.resources;

import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.learning.*;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


/**
 * IMPORTANT TODO:
 *   ordering of event requests to server
 *   example: skip A, start B send from android
 *            start B, skip A finished order at server due to different service times
 *
 *            find a way to not use history of the playlist (.previousSong())
 *
 */

/**
 * IMPORTANT: all events that cause learning to occur, i.e. call QLearner.update()
 * must also add the event to the playlist, asynchronously.
 */

@Path("/events/{userId}/{playlistId}")
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {
  private static final Logger log = Logger.getLogger(EventResource.class.getName());

  // TODO dynamically change based on some setting parameter
  // Change the object to choose the reward function.
  public static RewardFunction reward = new BinaryRewardFunction();
  public static Recommender recommender = new LearnedRecommender();

  @POST
  @Path("/start/{songId}")
  public Response trackStarted(@PathParam("userId") String userId,
                               @PathParam("playlistId") Long playlistId,
                               @PathParam("songId") String songId) {


    // update the listening history sliding window and other session state.
    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    playlist.nowPlaying(songId); // async

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    String t = "";
    for(String s : playlist.history()) t += s + " ";
    log.warning(t);

    return Response.ok(next).build();
  }

  @POST
  @Path("/finish/{songId}")
  public Response trackFinished(@PathParam("userId") String userId,
                                @PathParam("playlistId") Long playlistId,
                                @PathParam("songId") String songId) {

    double r = reward.rewardTrackFinished();
    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    Logger.getLogger("").warning("Last played: " + playlist.lastFinished());

    if(playlist.lastFinished() != null) {
      // record event in the events history for playlist
      EventHistory.addEnd(playlist.events(), playlist.lastFinished(), songId);

      if (playlist.lastFinished() != null) {
        LeafCluster previous = OfyUtils.loadLeafCluster(playlistId, playlist.lastFinished()).now();
        LeafCluster current = OfyUtils.loadLeafCluster(playlistId, songId).now();
        QLearner.update(previous, current, r);
      }
    }

    // update the last successful song played in the playlist
    playlist.justFinished(songId).now();

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok().entity(next).build();
  }

  @POST
  @Path("/skip/{songId}/{skipTime}")
  public Response skip(@PathParam("userId") String userId,
                       @PathParam("playlistId") Long playlistId,
                       @PathParam("songId") String songId,
                       @PathParam("skipTime") int skipTime) {
    double r = reward.rewardSkip(skipTime);

    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    Logger.getLogger("").warning("Last played: " + playlist.lastFinished());


    if(playlist.lastFinished() != null) {
      // record event in the events history for playlist
      EventHistory.addSkipped(playlist.events(), playlist.lastFinished(), songId, skipTime);
      ofy().save().entity(playlist).now(); // async

      LeafCluster previous = OfyUtils.loadLeafCluster(playlistId, playlist.lastFinished()).now();
      LeafCluster current = OfyUtils.loadLeafCluster(playlistId, songId).now();
      Logger.getLogger("s").warning(playlist.lastFinished() + "-->" + songId);
      QLearner.update(previous, current, r);
    }

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();

    return Response.ok().entity(next).build();
  }


  @POST
  @Path("/queue/{currentSongId}/{queuedSongId}")
  public Response queue(@PathParam("userId") String userId,
                        @PathParam("playlistId") Long playlistId,
                        @PathParam("currentSongId") String currentSongId,
                        @PathParam("queuedSongId") String queuedSongId) {

    double r = reward.rewardQueue();

    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    Logger.getLogger("").warning("Last played: " + playlist.lastFinished());

    if(playlist.lastFinished() != null) {
      // record event in the events history for playlist
      EventHistory.addQueued(playlist.events(), playlist.lastFinished(), queuedSongId);
      ofy().save().entity(playlist).now(); // async

      LeafCluster current = OfyUtils.loadLeafCluster(playlistId, currentSongId).now();
      LeafCluster queued = OfyUtils.loadLeafCluster(playlistId, queuedSongId).now();

      QLearner.update(current, queued, r);
    }

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok().entity(next).build();  }

  @POST
  @Path("/favourite/{songId}")
  public Response favourite(@PathParam("userId") String userId,
                            @PathParam("playlistId") Long playlistId,
                            @PathParam("songId") String songId) {
    double r = reward.rewardFavourite();

    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    Logger.getLogger("").warning("Last played: " + playlist.lastFinished());

    if(playlist.lastFinished() != null) {
      // record event in the events history for playlist
      EventHistory.addFavourite(playlist.events(), playlist.lastFinished(), songId);
      ofy().save().entity(playlist).now(); // async

      LeafCluster previous = OfyUtils.loadLeafCluster(playlistId, playlist.lastFinished()).now();
      LeafCluster current = OfyUtils.loadLeafCluster(playlistId, songId).now();

      QLearner.update(previous, current, r);
    }

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok().entity(next).build();
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response viewEventHistory(@PathParam("userId") String userId,
                                   @PathParam("playlistId") Long playlistId) {

    Playlist p = OfyUtils.loadPlaylist(userId, playlistId);
    StringBuilder sb = new StringBuilder();
    LinkedList<String> events = p.events();
    for(String event : events) {
      sb.append(event).append("\n");
    }
    return Response.ok().entity(sb.toString()).build();
  }
}
