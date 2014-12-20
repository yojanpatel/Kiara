package uk.co.yojan.kiara.analysis.resources;

import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.learning.*;
import uk.co.yojan.kiara.server.models.Playlist;
import uk.co.yojan.kiara.server.models.Song;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/events/{userId}/{playlistId}")
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {
  private static final Logger log = Logger.getLogger(EventResource.class.getName());

  // TODO dynamically change based on some setting parameter
  // Change the object to choose the reward function.
  public static RewardFunction reward = new BinaryRewardFunction();
  public static Recommender recommender = new ClusterRecommender();

  @POST
  @Path("/start/{songId}")
  public Response trackStarted(@PathParam("userId") String userId,
                               @PathParam("playlistId") Long playlistId,
                               @PathParam("songId") String songId) {


    // update the listening history sliding window and other session state.
    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    playlist.nowPlaying(songId);

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok().entity(next).build();
  }

  @POST
  @Path("/finish/{songId}")
  public Response trackFinished(@PathParam("userId") String userId,
                                @PathParam("playlistId") Long playlistId,
                                @PathParam("songId") String songId) {

    double r = reward.rewardTrackFinished();

    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    LeafCluster previous = OfyUtils.loadLeafCluster(playlistId, playlist.previousSong()).now();
    LeafCluster current = OfyUtils.loadLeafCluster(playlistId, songId).now();
    QLearner.update(previous, current, r);

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok().entity(next).build();
  }

  @POST
  @Path("/skip/{songId}/{skipTime}")
  public Response skip(@PathParam("userId") String userId,
                       @PathParam("playlistId") Long playlistId,
                       @PathParam("songId") String songId,
                       @PathParam("skipTime") int skipTime) {
    double r = reward.rewardSkip();

    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    LeafCluster previous = OfyUtils.loadLeafCluster(playlistId, playlist.previousSong()).now();
    LeafCluster current = OfyUtils.loadLeafCluster(playlistId, songId).now();

    QLearner.update(previous, current, r);

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

    LeafCluster current = OfyUtils.loadLeafCluster(playlistId, currentSongId).now();
    LeafCluster queued = OfyUtils.loadLeafCluster(playlistId, queuedSongId).now();

    QLearner.update(current, queued, r);

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok().entity(next).build();  }

  @POST
  @Path("/favourite/{songId}")
  public Response favourite(@PathParam("userId") String userId,
                            @PathParam("playlistId") Long playlistId,
                            @PathParam("songId") String songId) {
    double r = reward.rewardFavourite();

    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    LeafCluster previous = OfyUtils.loadLeafCluster(playlistId, playlist.previousSong()).now();
    LeafCluster current = OfyUtils.loadLeafCluster(playlistId, songId).now();

    QLearner.update(previous, current, r);

    Song next = OfyUtils.loadSong(recommender.recommend(userId, playlistId)).now();
    return Response.ok().entity(next).build();
  }
}
