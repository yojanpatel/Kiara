package uk.co.yojan.kiara.analysis.resources;

import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.learning.BinaryRewardFunction;
import uk.co.yojan.kiara.analysis.learning.RewardFunction;
import uk.co.yojan.kiara.server.models.Playlist;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/events/{userId}/{playlistId}")
public class EventResource {

  // TODO dynamically change based on some setting parameter
  // Change the object to choose the reward function.
  public static RewardFunction reward = new BinaryRewardFunction();

  @POST
  @Path("/start/{songId}")
  public Response trackStarted(@PathParam("userId") String userId,
                               @PathParam("playlistId") Long playlistId,
                               @PathParam("songId") String songId) {


    // update the listening history sliding window and other session state.
    Playlist playlist = OfyUtils.loadPlaylist(userId, playlistId);
    playlist.nowPlaying(songId);
    return Response.ok().build();
  }

  @POST
  @Path("/finish/{songId}")
  public Response trackFinished(@PathParam("songId") String songId) {

    // TODO get reward from RewardFunction and update the relevant Q matrices
    double r = reward.rewardTrackFinished();

    return Response.ok().build();
  }

  @POST
  @Path("/skip/{songId}/{skipTime}")
  public Response skip(@PathParam("songId") String songId,
                       @PathParam("skipTime") int skipTime) {

    // TODO get (negative) reward from RewardFunction and update the relevant Q matrices.
    double r = reward.rewardSkip();

    return Response.ok().build();
  }


  @POST
  @Path("/queue/{currentSongId}/{queuedSongId}")
  public Response queue(@PathParam("currentSongId") String currentSongId,
                        @PathParam("queuedSongId") String queuedSongId) {

    double r = reward.rewardQueue();

    return Response.ok().build();
  }

  @POST
  @Path("/favourite/{songId}")
  public Response favourite(@PathParam("songId") String songId) {
    double r = reward.rewardFavourite();
    return Response.ok().build();
  }
}
