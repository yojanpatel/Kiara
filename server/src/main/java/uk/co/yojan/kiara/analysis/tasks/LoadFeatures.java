package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;

import static uk.co.yojan.kiara.analysis.research.FeatureSelection.loadFeatures;

/**
 * Created by yojan on 12/29/14.
 */
public class LoadFeatures implements DeferredTask {

  String spotifyId;
  String artist;
  String title;

  public LoadFeatures(String spotifyId, String artist, String title) {
    this.spotifyId = spotifyId;
    this.artist = artist;
    this.title = title;
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
    loadFeatures(artist, title, spotifyId);
  }
}
