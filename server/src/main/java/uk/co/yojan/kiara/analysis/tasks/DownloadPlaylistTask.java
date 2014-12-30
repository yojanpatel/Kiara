package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.wrapper.spotify.exceptions.WebApiException;

import java.io.IOException;

import static uk.co.yojan.kiara.analysis.research.FeatureSelection.loadPlaylist;

/**
 * Created by yojan on 12/24/14.
 */
public class DownloadPlaylistTask implements DeferredTask {

  private String playlistUri;

  public DownloadPlaylistTask(String playlistUri) {
    this.playlistUri = playlistUri;
  }

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
    try {
      loadPlaylist(playlistUri);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (WebApiException e) {
      e.printStackTrace();
    }
  }
}
