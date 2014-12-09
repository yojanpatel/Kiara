package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import uk.co.yojan.kiara.server.echonest.EchoNestApi;
import uk.co.yojan.kiara.server.models.SongAnalysis;
import uk.co.yojan.kiara.server.models.SongData;

import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


public class GetSongAnalysisTask implements DeferredTask {

  private String spotifyId;
  private String artist;
  private String title;

  public GetSongAnalysisTask(String spotifyId, String artist, String title) {
    this.spotifyId = spotifyId;
    this.artist = artist;
    this.title = title;
  }

  private static final Logger log = Logger.getLogger(uk.co.yojan.kiara.analysis.tasks.GetSongAnalysisTask.class.getName());

  /**
   * When an object implementing interface Runnable is used
   * to create a thread, starting the thread causes the object's
   * run method to be called in that separately executing
   * thread.
   *
   * The general contract of the method run is that it may
   * take any action whatsoever.
   *
   */
  @Override
  public void run() {
    SongAnalysis songAnalysis = EchoNestApi.getSongAnalysis(spotifyId);

    // As a fallback, search EchoNest with the artist and title name of the song.
    // This often works for obscure or new tracks.
    if(songAnalysis == null) {
      log.info("Failed to search with the Spotify Id, trying to search using artist name and title.");
      songAnalysis = EchoNestApi.getSongAnalysis(artist, title);
    }
    songAnalysis.setId(spotifyId);
    SongData songData = songAnalysis.getSongData();
    songData.setSpotifyId(spotifyId);
    ofy().save().entities(songData, songAnalysis).now();
  }
}
