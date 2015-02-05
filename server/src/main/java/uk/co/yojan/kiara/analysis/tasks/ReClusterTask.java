package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.*;
import uk.co.yojan.kiara.analysis.learning.ActionEvent;
import uk.co.yojan.kiara.analysis.learning.QLearner;
import uk.co.yojan.kiara.analysis.learning.rewards.RewardFunction;
import uk.co.yojan.kiara.analysis.learning.rewards.VariedSkipReward;
import uk.co.yojan.kiara.server.models.Playlist;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import static uk.co.yojan.kiara.server.OfyService.ofy;

public class ReClusterTask implements DeferredTask {

  RewardFunction reward;

  NodeCluster root;
  Playlist p;
  Long playlistId;
  LinkedList<String> events;

  HashMap<Key<NodeCluster>, NodeCluster> nodes;
  HashMap<Key<LeafCluster>, LeafCluster> leaves;

  public ReClusterTask(Long playlistId) {
    this.playlistId = playlistId;
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
    reward = new VariedSkipReward();

    nodes = new HashMap<>();
    leaves = new HashMap<>();

    p = ofy().load().key(Key.create(Playlist.class, playlistId)).now();
    root = OfyUtils.loadRootCluster(playlistId).now();
    this.events = p.events();

    // Set intermediate playlist state (cluster is not ready for a minute or so)
    // also reset changes made since last recluster
    p.setLastClusterSize(p.getAllSongIds().size());
    p.setChangesSinceLastCluster(0);
    p.setClusterReady(false);
    Result r0 = ofy().save().entity(p);
    new PlaylistClusterer().cluster(playlistId, 9);
    r0.now();

    p.setClusterReady(true);
    ofy().save().entities(p);

    if(events.size() < 10) {
      ofy().save().entity(p).now();
      return;
    }

    QLearner qLearner = new QLearner();
    loadNodes(OfyUtils.loadRootCluster(playlistId).now());


    for(String event : events) {
      ActionEvent action = action(event);
      LeafCluster previous = leaves.get(Key.create(LeafCluster.class,
          ClusterUtils.clusterId(playlistId, action.getPreviousSongId())));
      LeafCluster current = leaves.get(Key.create(LeafCluster.class,
          ClusterUtils.clusterId(playlistId, action.getStartedSongId())));

      if(previous == null || current == null) continue;

      double r;
      if(action.isFavourited()) {
        r = reward.rewardFavourite();
      } else if(action.isSkipped()) {
        r = reward.rewardSkip(action.getPercentage());
      } else if(action.isQueued()) {
        r = reward.rewardQueue();
      } else {
        r = reward.rewardTrackFinished();
      }
      qLearner.optimizedUpdate(nodes, leaves, previous, current, r);
    }


    Result r1 = ofy().save().entities(nodes.values());
    Result r2 = ofy().save().entities(leaves.values());

    r0.now();
    r1.now();
    r2.now();
  }

  private ActionEvent action(String event) {
    ActionEvent e = new ActionEvent();
    String[] eventSections = event.split("-");
    String action = eventSections[2];

    e.setPreviousSongId(eventSections[0]);
    e.setStartedSongId(eventSections[1]);
    if(action.equals("END")) {
      e.setSkipped(false);
    } else if(action.equals("SKIP")) {
      e.setSkipped(true);
      e.setPercentage((int) Double.parseDouble(eventSections[3]));
    } else if(action.equals("FAVOURITE")) {
      e.setFavourited(true);
    } else if(action.equals("QUEUE")) {
      e.setQueued(true);
    }
    return e;
  }

  private void loadNodes(NodeCluster root) {
    nodes.put(Key.create(NodeCluster.class, root.getId()), root);
    Stack<Cluster> stack = new Stack<>();
    stack.add(root);
    while(!stack.isEmpty()) {
      Cluster c = stack.pop();
      if(c instanceof NodeCluster) {
        nodes.put(Key.create(NodeCluster.class, c.getId()), (NodeCluster) c);
        stack.addAll(((NodeCluster) c).getChildren());
      } else if(c instanceof LeafCluster) {
        leaves.put(Key.create(LeafCluster.class, c.getId()), (LeafCluster) c);
      }
    }
  }
}
