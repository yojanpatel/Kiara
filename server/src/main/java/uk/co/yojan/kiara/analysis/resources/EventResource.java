package uk.co.yojan.kiara.analysis.resources;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.learning.ActionEvent;
import uk.co.yojan.kiara.analysis.learning.EventHistory;
import uk.co.yojan.kiara.analysis.learning.QLearner;
import uk.co.yojan.kiara.analysis.learning.recommendation.BottomUpRecommender;
import uk.co.yojan.kiara.analysis.learning.recommendation.Recommender;
import uk.co.yojan.kiara.analysis.learning.rewards.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.rewards.VariedSkipReward;
import uk.co.yojan.kiara.server.Constants;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


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
  public static RewardFunction reward = new VariedSkipReward();
  public static Recommender recommender = new BottomUpRecommender();

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

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlist, playlist.lastFinished())).now();
    return Response.ok().entity(next).build();  }


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

    // TODO: uncomment when done with experimenting
    // Playlist p = OfyUtils.loadPlaylist(userId, playlistId);
    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    learnFromEvent(p, event).now();

    // Recommend song based on the last finished song.
    // If there is no previously finished song (recent), recommend based on the last song that was played
    String recentSongId = p.lastFinished();
    if(recentSongId == null) {
      recentSongId = p.previousSong();
    }

    Song recommended = OfyUtils.loadSong(recommender.recommend(userId, p, recentSongId)).now();
    return Response.ok(recommended).build();
  }

  @GET
  @Path("/recommend")
  public Response recommend(@PathParam("userId") String userId,
                            @PathParam("playlistId") Long playlistId,
                            @QueryParam("s") String songId) {

    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    Song recommended = OfyUtils.loadSong(recommender.recommend(userId, p, songId)).now();
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
    Result<LeafCluster> startedLeafClusterResult = OfyUtils.loadLeafCluster(playlistId, started);

    // Learn iff lastFinished --> justFinished transition exists.
    boolean transitionUpdatePossible = justFinished != null && lastFinished != null;
    if(transitionUpdatePossible) {
      double r = 0.0;

      // Boost reward if favourited.
      if(event.isFavourited()) {
        r += reward.rewardFavourite();
        EventHistory.addFavourite(playlist.events(), lastFinished, justFinished);
      }

      // Determine if skipped or finished naturally, this section regards learning
      // from the song that just finished.
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

      // Determine if the new song that is playing was queued. In this case,
      // need to emit reward for the queuing action.
      if(event.isQueued()) {
        // Learn with respect to the song that will have just finished.
        LeafCluster finished = (!event.isSkipped() || event.getPercentage() > Constants.SKIP_SONG_FINISH)
                                  ? currentLeaf : previousLeaf;

        QLearner.update(finished, startedLeafClusterResult.now(), reward.rewardQueue());
      }
    }

    if((!event.isSkipped() || event.getPercentage() > Constants.SKIP_SONG_FINISH) && justFinished != null) {
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
