package uk.co.yojan.kiara.analysis.cluster;

import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.tasks.BatchAddSongTask;
import uk.co.yojan.kiara.analysis.tasks.ReClusterTask;
import uk.co.yojan.kiara.analysis.tasks.TaskManager;
import uk.co.yojan.kiara.server.models.Playlist;

import java.util.logging.Logger;

import static uk.co.yojan.kiara.server.OfyService.ofy;


public class ClusterUpdateUtil {

  public static Logger log = Logger.getLogger("ClusterUpdateUtil");

  public static void updateOnAdd(Long playlistId, String[] ids) {
    Playlist p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    if (p.needToRecluster()) {
      log.warning("Playlist has to be reclustered. Task added to the cluster queue.");
      TaskManager.clusterQueue().add(TaskOptions.Builder
          .withPayload(new ReClusterTask(playlistId))
          .retryOptions(RetryOptions.Builder.withTaskRetryLimit(2).minBackoffSeconds(10).maxBackoffSeconds(10))
          .taskName("ReCluster-" + playlistId + "-" + System.currentTimeMillis()));
    } else {
      log.warning("Adding based on a greedy approach.");
      // ad-hoc: update the playlist cluster representation
      TaskManager.clusterQueue().add(TaskOptions.Builder
          .withPayload(new BatchAddSongTask(ids, playlistId))
          .retryOptions(RetryOptions.Builder.withTaskRetryLimit(2).minBackoffSeconds(10).maxBackoffSeconds(10))
          .taskName("AddSongs-" + playlistId + "-" + System.currentTimeMillis()));
    }
  }

  public static void updateOnAdd(Playlist p, String[] ids) {
    if (p.needToRecluster()) {
      log.warning("Playlist has to be reclustered. Task added to the cluster queue.");
      TaskManager.clusterQueue().add(TaskOptions.Builder
          .withPayload(new ReClusterTask(p.getId()))
          .retryOptions(RetryOptions.Builder.withTaskRetryLimit(2).minBackoffSeconds(10).maxBackoffSeconds(10))
          .taskName("ReCluster-" + p.getId() + "-" + System.currentTimeMillis()));
    } else {
      log.warning("Adding based on a greedy approach.");
      // ad-hoc: update the playlist cluster representation
      TaskManager.clusterQueue().add(TaskOptions.Builder
          .withPayload(new BatchAddSongTask(ids, p.getId()))
          .retryOptions(RetryOptions.Builder.withTaskRetryLimit(2).minBackoffSeconds(10).maxBackoffSeconds(10))
          .taskName("AddSongs-" + p.getId() + "-" + System.currentTimeMillis()));
    }
  }
}
