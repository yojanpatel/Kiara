package uk.co.yojan.kiara.server.tasks;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.util.logging.Logger;


public class TaskManager {

  private static Logger log = Logger.getLogger(TaskManager.class.getName());

  private static Queue taskQueue = null;

  private TaskManager() {}

  public static Queue getQueue() {
    if(taskQueue == null) {
      taskQueue = QueueFactory.getDefaultQueue();
    }
    return taskQueue;
  }

  public static void fetchAnalysis(String spotifyId) {
    log.info("Adding new GetSongAnalysisTask to the task queue for id: " + spotifyId);
    GetSongAnalysisTask task = new GetSongAnalysisTask(spotifyId);
    getQueue().add(TaskOptions.Builder.withPayload(task).taskName("FetchAnalysis-" + spotifyId));
  }
}
