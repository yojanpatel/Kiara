package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.Cluster;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;

import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

public class RemoveSongTask implements DeferredTask {


  Long playlistId;
  String songId;

  public RemoveSongTask(Long playlistId, String songId) {
    this.playlistId = playlistId;
    this.songId = songId;
  }

  @Override
  public void run() {
    NodeCluster root = OfyUtils.loadRootCluster(playlistId).now();

    while(true) {

      int indexToDelete = root.clusterIndex(songId);

      // degglomerate
      if(root.getChildIds().size() == 2) {
        Result<NodeCluster> r = ofy().load().key(root.getParent());

        int indexToKeep = indexToDelete == 0 ? 1 : 0;

        Cluster replacement = ofy().load().key(Key.create(LeafCluster.class, root.getChildIds().get(indexToKeep))).now();
        if(replacement == null) {
          replacement = ofy().load().key(Key.create(NodeCluster.class, root.getChildIds().get(indexToKeep))).now();
        }
        
        ofy().delete().key(Key.create(LeafCluster.class, leafId(playlistId, songId)));
        NodeCluster parent = r.now();
        degglomerate(parent, root, replacement);

        return;
      }

      root.removeSongId(songId);

      if (root.containsLeaf(leafId(playlistId, songId))) {

        // delete appropriate entry for the leaf in the Q matrix
        List<List<Double>> Q = root.getQ();
        Q.remove(indexToDelete);
        for(List<Double> stateRow : Q) {
          stateRow.remove(indexToDelete);
        }

        // remove from children
        root.getChildIds().remove(indexToDelete);

        ofy().delete().key(Key.create(LeafCluster.class, leafId(playlistId, songId)));
        return;
      } else {
        root =  OfyUtils.loadNodeCluster(root.getChildIds().get(indexToDelete)).now();
      }
    }
  }

  // parent -> clusterToRemove -> replacement
  //   to
  // parent -> replacement
  private void degglomerate(NodeCluster parent, NodeCluster clusterToRemove, Cluster replacement ) {
    int removeIndex = parent.nodeClusterIndex(clusterToRemove.getId());

    parent.getChildIds().set(removeIndex, replacement.getId());

    if(replacement instanceof LeafCluster) {
      ((LeafCluster)replacement).setParent(Key.create(NodeCluster.class, parent.getId()));
      parent.addLeaf(replacement.getId());

    } else if(replacement instanceof NodeCluster) {
      ((NodeCluster)replacement).setParent(Key.create(NodeCluster.class, parent.getId()));
    }

    replacement.setLevel(replacement.getLevel() - 1);
    ofy().save().entities(parent).now();
    ofy().save().entities(replacement).now();
    ofy().delete().entity(clusterToRemove);
  }

  private String leafId(Long playlistId, String songId) {
    return playlistId + "-" + songId;
  }
}
