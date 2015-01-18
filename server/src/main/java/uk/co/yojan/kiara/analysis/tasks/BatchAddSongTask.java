package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Result;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.Cluster;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.analysis.cluster.KMeans.getAttributeNames;
import static uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer.clusterId;
import static uk.co.yojan.kiara.server.OfyService.ofy;


public class BatchAddSongTask implements DeferredTask {

  private String[] songIds;
  private Long playlistId;

  private EuclideanDistance distanceFunction;
  private Instances instances;

  HashMap<Key<NodeCluster>, NodeCluster> nodes;
  HashMap<Key<LeafCluster>, LeafCluster> leaves;
  Map<Key<SongFeature>, SongFeature> songFeatures;

  boolean incomplete;

  public BatchAddSongTask(String[] songIds, Long playlistId) {
    this.songIds = songIds;
    this.playlistId = playlistId;
    nodes = new HashMap<>();
    leaves = new HashMap<>();
    songFeatures = new HashMap<>();
  }

  @Override
  public void run() {
    loadFeatures(songIds);

    NodeCluster rootCluster = OfyUtils.loadRootCluster(playlistId).now();

    for(String songId : songIds) {

      if(rootCluster.getSongIds().contains(songId)) {
        Logger.getLogger("").warning("Already contains it!");
        continue;
      }

      SongFeature song = loadFeature(songId);
      if (song == null) {
        incomplete = true;
        continue;
      }

      while (true) {
        rootCluster.getChildIds();
        List<Cluster> children = getChildren(rootCluster);

        Cluster closestCluster = rootCluster;
        Double closestCentroidDistance = Double.MAX_VALUE;

        for (Cluster child : children) {
          double d = Integer.MAX_VALUE;

          try {

            // Two cases: NodeCluster centroid OR singleton LeafCluster
            if (child instanceof NodeCluster) {
              d = distance(
                  new Instance(1.0, song.getFeatureValues()),
                  new Instance(1.0, ((NodeCluster) child).getCentroidValues()));
            } else if (child instanceof LeafCluster) {
              SongFeature leaf = OfyUtils.loadFeature(((LeafCluster) child).getSongId()).now();
              d = distance(
                  new Instance(1.0, leaf.getFeatureValues()),
                  new Instance(1.0, song.getFeatureValues()));
            }
          } catch (IllegalAccessException e) {
            Logger.getLogger("").warning(e.getMessage());
          }

          // update result if found
          if (d < closestCentroidDistance) {
            closestCentroidDistance = d;
            closestCluster = child;
          }
        }

        if (closestCluster instanceof LeafCluster) {

          // agglomerate the leaf cluster and the song to be added
          agglomerateCluster((LeafCluster) closestCluster, song);
          return;

        } else if (closestCluster instanceof NodeCluster) {

          // update the clusters along the path's shadow
          rootCluster.addSongId(song.getId());
          nodes.put(Key.create(NodeCluster.class, rootCluster.getId()), rootCluster);

          // recursively add to the child cluster
          rootCluster = (NodeCluster) closestCluster;
        }
      }
    }
    saveAll();
    if(incomplete) {
      throw new NullPointerException("No analysis found for some songs. Will retry later.");
    }
  }

  // parent -> leaf
  //
  // parent -> agglomerated -> {leaf, newLeaf}
  private void agglomerateCluster(LeafCluster leaf, SongFeature song) {
    // agglomerated new NodeCluster
    NodeCluster agglomerated = new NodeCluster();

    // create new LeafCluster for the new song and add to agglomeration
    LeafCluster newLeaf = new LeafCluster(song.getId());
    newLeaf.setId(playlistId + "-" + song.getId());

    // parent node
    // update parent link for the new node
    agglomerated.setParent(leaf.getParent());
    NodeCluster parent = loadNode(agglomerated.getParent());

    // set the id based on the parent
    Logger.getLogger("").warning(parent.getId() + " " + parent.childIndex(leaf) + "  " + parent.getSize());
    agglomerated.setId(clusterId(parent, parent.childIndex(leaf), parent.getSize()));

    // replace child link for the parent to the new agglomerated node instead of the singleton
    parent.replaceChild(leaf, agglomerated);

    agglomerated.setLevel(parent.getLevel() + 1);

    // add the existing singleton to this agglomeration
    agglomerated.addChild(leaf);
    leaf.setLevel(agglomerated.getLevel() + 1);


    newLeaf.setLevel(agglomerated.getLevel() + 1);
    agglomerated.addChild(newLeaf);

    // update existing and new leafclusters parent entry
    Key<NodeCluster> agglomeratedKey = Key.create(NodeCluster.class, agglomerated.getId());
    newLeaf.setParent(agglomeratedKey);
    leaf.setParent(agglomeratedKey);

    nodes.put(Key.create(NodeCluster.class, parent.getId()), parent);
    nodes.put(Key.create(NodeCluster.class, agglomerated.getId()), agglomerated);
    leaves.put(Key.create(LeafCluster.class, newLeaf.getId()), newLeaf);
    leaves.put(Key.create(LeafCluster.class, leaf.getId()), leaf);
  }


  private double distance(Instance first, Instance second) {
    if(distanceFunction == null) {
      distanceFunction = new EuclideanDistance();
    }
    if(instances == null) {
      instances = new Instances("Clusters", getAttributeNames(), 2);
      distanceFunction.setInstances(instances);
    }

    instances.delete();
    instances.add(first);
    instances.add(second);

    return distanceFunction.distance(instances.firstInstance(), instances.lastInstance());
  }

  private LeafCluster loadLeaf(Key<LeafCluster> key) {
    if(leaves.containsKey(key)) {
      return leaves.get(key);
    } else {
      LeafCluster leaf = ofy().load().key(key).now();
      leaves.put(key, leaf);
      return leaf;
    }
  }

  private NodeCluster loadNode(Key<NodeCluster> key) {
    if(nodes.containsKey(key)) {
      return nodes.get(key);
    } else {
      NodeCluster node = ofy().load().key(key).now();
      nodes.put(key, node);
      return node;
    }
  }

  private void loadFeatures(String[] songIds) {
    List<Key<SongFeature>> keys = new ArrayList<>();
    for(String id : songIds) keys.add(Key.create(SongFeature.class, id));
    songFeatures = ofy().load().keys(keys);
  }

  private SongFeature loadFeature(String id) {
    return songFeatures.get(Key.create(SongFeature.class, id));
  }

  List<Cluster> getChildren(NodeCluster cluster) {
    List<Cluster> children = new ArrayList<>();
    List<String> childIds = cluster.getChildIds();
    for(String id : childIds) {
      if(cluster.containsLeaf(id)) {
        children.add(loadLeaf(Key.create(LeafCluster.class, id)));
      } else {
        children.add(loadNode(Key.create(NodeCluster.class, id)));
      }
    }
    return children;
  }

  private void saveAll() {
    Result r1 = ofy().save().entities(nodes.values());
    Result r2 = ofy().save().entities(leaves.values());
    r1.now();
    r2.now();
  }
}
