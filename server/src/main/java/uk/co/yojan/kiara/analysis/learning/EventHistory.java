package uk.co.yojan.kiara.analysis.learning;

import javafx.util.Pair;
import uk.co.yojan.kiara.server.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * A set of methods to handle a Queue<String> that holds a sliding window
 * of events recorded to allow re-learning if the structure changes.
 */
public class EventHistory {

  /*
   * Event String representation:
   *   source_id-target_id-action[-extra]
   */

  private static int EVENT_HISTORY_SIZE = -1;

  public static int size() {
    if(EVENT_HISTORY_SIZE == -1) {
      return Constants.EVENT_HISTORY_SIZE;
    }
    return EVENT_HISTORY_SIZE;
  }

  public static void addEnd(LinkedList<String> eventHistory, String previousSongId, String endedSongId) {
    // if new session, no previous song.
    if(previousSongId == null) return;

    if(eventHistory.size() >= size()) {
      eventHistory.removeFirst();
    }

    eventHistory.addLast(previousSongId + "-" + endedSongId + "-" + PlayerEvent.END);
  }

  public static void addSkipped(LinkedList<String> eventHistory, String previousSongId, String skippedSongId, double proportion) {
    // if new session, no previous song.
    if(previousSongId == null) return;

    if(eventHistory.size() >= size()) {
      eventHistory.removeFirst();
    }
    // rough round to 2dp
    eventHistory.addLast(previousSongId + "-" + skippedSongId + "-" + PlayerEvent.SKIP + "-" + round2dp(proportion));
  }

  public static void addQueued(LinkedList<String> eventHistory, String previousSongId, String queuedSongId) {
    // if new session, no previous song.
    if(previousSongId == null) return;

    if(eventHistory.size() >= size()) {
      eventHistory.removeFirst();
    }

    eventHistory.add(previousSongId + "-" + queuedSongId + "-" + PlayerEvent.QUEUE);
  }

  public static void addFavourite(LinkedList<String> eventHistory, String previousSongId, String favSongId) {
    // if new session, no previous song.
    if(previousSongId == null) return;

    if(eventHistory.size() >= size()) {
      eventHistory.removeFirst();
    }
    eventHistory.add(previousSongId + "-" + favSongId + "-" + PlayerEvent.FAVOURITE);
  }

  public static int getEventHistorySize() {
    return size();
  }

  public static void setEventHistorySize(int eventHistorySize) {
    EVENT_HISTORY_SIZE = eventHistorySize;
  }

  public static ArrayList<Pair<Integer, Integer>> similar(LinkedList<String> eventHistory, HashMap<String, Integer> index) {

    ArrayList<Pair<Integer, Integer>> similar = new ArrayList<>();
    for(String event : eventHistory) {
      if(event.contains("FAVOURITE")) {
        String[] s = event.split("-");
        similar.add(new Pair<>(index.get(s[0]), index.get(s[1])));
      }
    }
    return similar;
  }

  public static  ArrayList<Pair<Integer, Integer>> different(LinkedList<String> eventHistory, HashMap<String, Integer> index) {
    ArrayList<Pair<Integer, Integer>> different = new ArrayList<>();
    for(String event : eventHistory) {
      if(event.contains("SKIP")) {
        String[] s = event.split("-");
        if(Integer.parseInt(s[3]) < 10) {
          different.add(new Pair<>(index.get(s[0]), index.get(s[1])));
        }
      }
    }
    return different;
  }




  private static double round2dp(double d) {
    return Math.round(d * 100)/100.0;
  }

  public static void main(String[] args) {
    LinkedList<String> history = new LinkedList<>();
    addSkipped(history, "X", "A", 0.5);
    addEnd(history, "A", "B");
    addFavourite(history, "B", "C");
    addEnd(history, "B", "C");

    for(String s : history) {
      System.out.println(s);
    }
  }
}
