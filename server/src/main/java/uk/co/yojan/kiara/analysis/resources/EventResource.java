package uk.co.yojan.kiara.analysis.resources;

import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.learning.*;
import uk.co.yojan.kiara.analysis.learning.recommendation.Recommender;
import uk.co.yojan.kiara.analysis.learning.recommendation.TopDownRecommender;
import uk.co.yojan.kiara.analysis.learning.rewards.BinaryRewardFunction;
import uk.co.yojan.kiara.analysis.learning.rewards.RewardFunction;
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
  private static QLearner QLearner = new QLearner();

  // TODO dynamically change based on some setting parameter
  // Change the object to choose the reward function.
  public static RewardFunction reward = new BinaryRewardFunction();
  public static Recommender recommender = new TopDownRecommender();

  @POST
  @Path("/start/{songId}")
  public Response trackStarted(@PathParam("userId") String userId,
                               @PathParam("playlistId") Long playlistId,
                               @PathParam("songId") String songId) {

    long t1 = System.currentTimeMillis();
    // update the listening history sliding window and other session state.
    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    long t2 = System.currentTimeMillis();
    playlist.nowPlaying(songId); // async
    long t3 = System.currentTimeMillis();
    Result save = ofy().save().entity(playlist);
    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    long t4 = System.currentTimeMillis();
    save.now();
    Logger.getLogger("").warning((t2 - t1) + " " + (t3 - t2) + " " + (t4 - t3));

    return Response.ok(next).build();
  }

  @POST
  @Path("/finish/{songId}")
  public Response trackFinished(@PathParam("userId") String userId,
                                @PathParam("playlistId") Long playlistId,
                                @PathParam("songId") String songId) {

    double r = reward.rewardTrackFinished();
    long t1 = System.currentTimeMillis();
    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    Logger.getLogger("").warning("Last played: " + playlist.lastFinished());
    long t2 = System.currentTimeMillis();
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
    playlist.justFinished(songId);
    long t3 = System.currentTimeMillis();
    Result save = ofy().save().entity(playlist);
    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    long t4 = System.currentTimeMillis();
    save.now(); // sync
    Logger.getLogger("").warning((t2 - t1) + " " + (t3 - t2) + " " + (t4 - t3));
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
      long t1 = System.currentTimeMillis();
      LeafCluster previous = OfyUtils.loadLeafCluster(playlistId, playlist.lastFinished()).now();
      LeafCluster current = OfyUtils.loadLeafCluster(playlistId, songId).now();
      long t2 = System.currentTimeMillis();
      Logger.getLogger("").warning("time taken to load leafclusters" + (t2-t1));
      Logger.getLogger("s").warning(playlist.lastFinished() + "-->" + songId);

      long t3 = System.currentTimeMillis();
      QLearner.update(previous, current, r);
      long t4 = System.currentTimeMillis();
      Logger.getLogger("").warning("time taken to update " + (t4-t3));
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

  /**
   * An event represents a transition in the music player listening.
   *
   * This conflates a lot of functionality that was previously seperated as different HTTP requests,
   * causing a lot of ordering issues and redundant store/gets of playlists, clusters, updates etc.
   *
   * To disambiguate various variables:
   *   -- lastFinished() is the last successful song to complete playback.
   *      It is against this song, that all the learning is measured relative against. (PREVIOUS)
   *   -- getEndedSongId() is the song that just completed playing, the source of the transition event. (CURRENT)
   *   -- getStartedSongId() is the song that has just started playing, the destination of the transition event. (NEXT)
   *
   *   TODO: refactor into individual methods
   *
   * @param userId
   * @param playlistId
   * @param event
   * @return
   */
  @POST
  public Response transitionEvent(@PathParam("userId") String userId,
                                  @PathParam("playlistId") Long playlistId,
                                  ActionEvent event) {
    // essentially does all of skip/finish, fav and start.
    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    learnFromEvent(playlist, event).now();
    Song recommended = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok(recommended).build();
  }


  public static Result learnFromEvent(Playlist playlist, ActionEvent event) {
    long playlistId = playlist.getId();

    // lastFinished ==  null: nothing to learn about song transition preferences, may need to update lastFinished.
    String lastFinished = playlist.lastFinished();
    // justFinished == null: similarly, nothing to learn and don't need to update lastFinished.
    String justFinished = event.getPreviousSongId();
    // started == null: should not happen.
    String started = event.getStartedSongId();
    assert started != null;

    // Learn iff lastFinished --> justFinished transition exists.
    boolean transitionUpdatePossible = justFinished != null && lastFinished != null;
    if(transitionUpdatePossible) {
      double r = 0.0;

      // Boost reward if favourited.
      if(event.isFavourited()) {
        r += reward.rewardFavourite();
        EventHistory.addFavourite(playlist.events(), lastFinished, justFinished);
      }

      // Determine if skipped or finished naturally.
      if(event.isSkipped()) {
        r += reward.rewardSkip(event.getPercentage());
        EventHistory.addSkipped(playlist.events(), lastFinished, justFinished, event.getPercentage());
      } else {
        r+= reward.rewardTrackFinished();
        EventHistory.addEnd(playlist.events(), lastFinished, justFinished);
      }
      Result<LeafCluster> result = OfyUtils.loadLeafCluster(playlistId, lastFinished);
      LeafCluster currentLeaf = OfyUtils.loadLeafCluster(playlistId, justFinished).now();
      LeafCluster previousLeaf = result.now();

      // the total reward is augmented by favouriting.
      QLearner.update(previousLeaf, currentLeaf, r);
    }

    if((!event.isSkipped() || event.getPercentage() > 80) && justFinished != null) {
      playlist.justFinished(justFinished);
    }
    playlist.nowPlaying(started);
    return ofy().save().entity(playlist);
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


  /** Setter methods to inject different Reward, Recommender Functions for Unit Testing. **/
  public static void setReward(RewardFunction reward) {
    EventResource.reward = reward;
  }

  public static void setRecommender(Recommender recommender) {
    EventResource.recommender = recommender;
  }
}
