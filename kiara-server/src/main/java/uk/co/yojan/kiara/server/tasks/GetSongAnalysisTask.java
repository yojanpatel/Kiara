package uk.co.yojan.kiara.server.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.SongData;

import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


public class GetSongAnalysisTask implements DeferredTask {

  private String spotifyId;

  public GetSongAnalysisTask(String spotifyId) {
    this.spotifyId = spotifyId;
  }

  private static final Logger log = Logger.getLogger(GetSongAnalysisTask.class.getName());

  /**
   * When an object implementing interface <code>Runnable</code> is used
   * to create a thread, starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p/>
   * The general contract of the method <code>run</code> is that it may
   * take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run() {
    SongAnalysis songAnalysis = EchoNestApi.getSongAnalysis(spotifyId);
    songAnalysis.setId(spotifyId);
    SongData songData = songAnalysis.getSongData();
    songData.setSpotifyId(spotifyId);
    ofy().save().entities(songData, songAnalysis).now();
  }
}
