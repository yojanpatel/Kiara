package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.DeferredTaskContext;
import uk.co.yojan.kiara.analysis.cluster.FeaturesNotReadyException;
import uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer;

import java.util.logging.Logger;


public class PlaylistClusterTask implements DeferredTask {

  private static final Logger log = Logger.getLogger(PlaylistClusterTask.class.getName());

  private Long playlistId;
  private int k;

  public PlaylistClusterTask(Long playlistId, int k) {
    this.k = k;
    this.playlistId = playlistId;
  }

  @Override
  public void run() {
    try {
      new PlaylistClusterer().cluster(playlistId, k);
    } catch (Exception e) {
      e.printStackTrace();
      log.warning(e.getMessage());
    } catch (FeaturesNotReadyException e) {
      DeferredTaskContext.markForRetry();
    }
  }
}
