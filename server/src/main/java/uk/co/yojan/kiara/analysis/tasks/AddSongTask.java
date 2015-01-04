package uk.co.yojan.kiara.analysis.tasks;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.googlecode.objectify.Key;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.Cluster;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.server.models.SongFeature;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.logging.Logger;

import static uk.co.yojan.kiara.analysis.cluster.KMeans.getAttributeNames;
import static uk.co.yojan.kiara.analysis.cluster.PlaylistClusterer.clusterId;
import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Created by yojan on 12/29/14.
 */
public class AddSongTask implements DeferredTask {

  private String songId;
  private Long playlistId;

  private EuclideanDistance distanceFunction;
  private Instances instances;

  public AddSongTask(String songId, Long playlistId) {
    this.songId = songId;
    this.playlistId = playlistId;
  }



  @Override
  public void run() {
    NodeCluster rootCluster = OfyUtils.loadRootCluster(playlistId).now();

    // only a single instance of a song allowed in a playlist, multiple jobs may be triggered in exceptional circumstances.
//    if(rootCluster.getSongIds().contains(songId)) {
//      Logger.getLogger("").warning("Already contains it!");
//      return;
//    }

    SongFeature song = OfyUtils.loadFeature(songId).now();
    if(song == null) {
      throw new NullPointerException("No analysis found for song " + songId + ". Will retry later.");
    }


    while(true) {
      List<Cluster> children = rootCluster.getChildren();

      Cluster closestCluster = rootCluster;
      Double closestCentroidDistance = Double.MAX_VALUE;

      for(Cluster child : children) {
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
        if(d < closestCentroidDistance) {
          closestCentroidDistance = d;
          closestCluster = child;
        }
      }

      // if number of children is less than allowed (i.e. K), check if this song should form its own cluster
      // this is done by reclustering the shadow of this node
      int addToClusterId = cluster(rootCluster, song);


      if(closestCluster instanceof LeafCluster) {

        // agglomerate the leaf cluster and the song to be added
        agglomerateCluster((LeafCluster) closestCluster, song);
        return;

      } else if(closestCluster instanceof NodeCluster) {

        // update the clusters along the path's shadow
        rootCluster.addSongId(song.getId());
        ofy().save().entity(rootCluster);

        // recursively add to the child cluster
        rootCluster = (NodeCluster) closestCluster;
      }
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
    NodeCluster parent = ofy().load().key(agglomerated.getParent()).now();

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

    ofy().save().entity(parent);
    ofy().save().entity(agglomerated);
    ofy().save().entity(newLeaf);
    ofy().save().entity(leaf);
  }

  //
  private int cluster(NodeCluster root, SongFeature song) {
//    List<SongFeature> features = new ArrayList<>(ofy().load().keys(featureKeys(cluster.getSongIds())).values());

    // Perform K-Means using Weka on the feature set returning a mapping
//    KMeans kMeans = new KMeans(k, features);
//    int[] assignments = kMeans.run();
    return 0;
  }


  public static void main(String[] args) {
    EuclideanDistance d = new EuclideanDistance();
    FastVector fv = new FastVector();
    Instances is = new Instances("distance", fv, 2);
    double[] d1 = {2.0, 3.0};
    double[] d2 = {3.0, 3.0};
    is.add(new Instance(1.0, d1));
    is.add(new Instance(1.0, d2));
    d.setInstances(is);

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

}
