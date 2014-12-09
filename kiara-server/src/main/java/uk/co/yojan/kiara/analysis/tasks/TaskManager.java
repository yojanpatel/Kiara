package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.util.logging.Logger;


public class TaskManager {

  private static Logger log = Logger.getLogger(uk.co.yojan.kiara.analysis.tasks.TaskManager.class.getName());

  private static Queue taskQueue = null;
  private static Queue featureQueue = null;
  private static Queue clusterQueue = null;

  private TaskManager() {}

  public static Queue getQueue() {
    if(taskQueue == null) {
      taskQueue = QueueFactory.getDefaultQueue();
    }
    return taskQueue;
  }

  public static Queue featureQueue() {
    if(featureQueue == null) {
      featureQueue = QueueFactory.getQueue("features");
    }
    return featureQueue;
  }

  public static Queue clusterQueue() {
    if(clusterQueue == null) {
      clusterQueue = QueueFactory.getQueue("cluster");
    }
    return clusterQueue;
  }

  public static void fetchAnalysis(String spotifyId, String artist, String title) {
    log.info("Adding new GetSongAnalysisTask to the task queue for id: " + spotifyId);
    uk.co.yojan.kiara.analysis.tasks.GetSongAnalysisTask task = new GetSongAnalysisTask(spotifyId, artist, title);
    featureQueue().add(TaskOptions.Builder
        .withPayload(task)
        .retryOptions(RetryOptions.Builder.withTaskRetryLimit(10))
        .taskName("FetchAnalysis-" + spotifyId));
  }
}
