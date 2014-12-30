package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;


public class TaskManager {

  private static Logger log = Logger.getLogger(uk.co.yojan.kiara.analysis.tasks.TaskManager.class.getName());

  private static Queue taskQueue = null;
  private static Queue updateQueue = null;
  private static Queue featureQueue = null;
  private static Queue clusterQueue = null;

  private TaskManager() {}

  public static Queue getQueue() {
    if(taskQueue == null) {
      taskQueue = QueueFactory.getDefaultQueue();
    }
    return taskQueue;
  }

  public static Queue updateQueue() {
    if(updateQueue == null) {
      updateQueue = QueueFactory.getQueue("updater");
    }
    return updateQueue;
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
    featureQueue().add(withUrl("/features")
        .param("id", spotifyId)
        .param("artist", artist)
        .param("title", title)
        .method(TaskOptions.Method.GET)
        .header("Host", ModulesServiceFactory.getModulesService().getVersionHostname("kiara-analysis", "v1")));
        Logger.getGlobal().info(ModulesServiceFactory.getModulesService().getVersionHostname("kiara-analysis", "v1"));
  }
}
