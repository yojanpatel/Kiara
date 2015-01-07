package uk.co.yojan.kiara.analysis.resources;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import junit.framework.TestCase;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;
import uk.co.yojan.kiara.analysis.learning.ActionEvent;
import uk.co.yojan.kiara.analysis.learning.Recommender;
import uk.co.yojan.kiara.analysis.learning.RewardFunction;
import uk.co.yojan.kiara.server.models.Playlist;

import java.util.LinkedList;
import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

/**
 * Pretty much an integration test that checks if a playlist's session state is updated,
 * the associated cluster and Q matrices are updated etc.
 */
public class EventResourceTest extends TestCase {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  NodeCluster A, B, C;
  LeafCluster l1, l2, l3, l4;
  ActionEvent a1, a2, a3, a4, a5;
  Playlist p;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    helper.setUp();

    // Inject Reward/Recommender functions for easier testing

    EventResource.setReward(new RewardFunction() {
      @Override
      public double rewardSkip(int percent) {
        return -1;
      }

      @Override
      public double rewardQueue() {
        return 1;
      }

      @Override
      public double rewardTrackFinished() {
        return 1;
      }

      @Override
      public double rewardFavourite() {
        return 1;
      }
    });
    EventResource.setRecommender(new Recommender() {
        @Override
        public String recommend(String userId, Long playlistId) {
          return null;
        }
      });

    A = createTestHierarchy();
    B = OfyUtils.loadNodeCluster("B").now();
    C = OfyUtils.loadNodeCluster("C").now();

    l1 = OfyUtils.loadLeafCluster("37-l1").now();
    l2 = OfyUtils.loadLeafCluster("37-l2").now();
    l3 = OfyUtils.loadLeafCluster("37-l3").now();
    l4 = OfyUtils.loadLeafCluster("37-l4").now();

    p = new Playlist();
    p.setId(37L);
    ofy().save().entity(p).now();

    // a1 = l3 started to play initially.
    a1 = new ActionEvent();
    a1.setStartedSongId("l3");

    // a2 = l3 skipped midway (50%), l4 starts to play.
    a2 = new ActionEvent();
    a2.setPreviousSongId("l3");
    a2.setStartedSongId("l4");
    a2.setSkipped(true);
    a2.setPercentage(50);

    // l4 finished playing, l2 started
    a3 = new ActionEvent();
    a3.setPreviousSongId("l4");
    a3.setStartedSongId("l2");

    // l2 just finished after being favourited
    a4 = new ActionEvent();
    a4.setPreviousSongId("l2");
    a4.setFavourited(true);
    a4.setStartedSongId("l1");

    // l1 was skipped and fav'd, lastFinished is still l2
    a5 = new ActionEvent();
    a5.setPreviousSongId("l1");
    a5.setSkipped(true);
    a5.setFavourited(true);
    a5.setPercentage(50);
    a5.setStartedSongId("l3");
  }

  @Override
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  public void testSessionStateUpdated() throws Exception {

    EventResource.learnFromEvent(p, a1);
    assertNull(p.lastFinished());
    assertEquals("l3", p.previousSong());

    EventResource.learnFromEvent(p, a2);
    assertNull(p.lastFinished());
    assertEquals("l4", p.previousSong());

    EventResource.learnFromEvent(p, a3);
    assertEquals("l4", p.lastFinished());
    assertEquals("l2", p.previousSong());

    // event history, currently should be none.
    // no learning-worthy events have yet happened.
    LinkedList<String> events = p.events();
    assertEquals(0, events.size());

    EventResource.learnFromEvent(p, a4);
    assertEquals("l2", p.lastFinished());
    assertEquals("l1", p.previousSong());

    // should reflect the events related to l2 being favourited and finishing after l4.
    events = p.events();
    assertEquals(2, events.size());
    assertEquals("l4-l2-FAVOURITE", events.getFirst());
    assertEquals("l4-l2-END", events.getLast());

    EventResource.learnFromEvent(p, a5);
    assertEquals(4, events.size());
    assertEquals("l2", p.lastFinished());
    assertEquals("l3", p.previousSong());
    assertEquals("l2-l1-FAVOURITE", events.get(2));
    assertEquals("l2-l1-SKIP-50.0",events.getLast());
  }

  public void testEventLearning() throws Exception {

    EventResource.learnFromEvent(p, a1);
    EventResource.learnFromEvent(p, a2);
    EventResource.learnFromEvent(p, a3);

    // no change yet
    double[] id2 = {1.0, 0.0, 0.0, 1.0};
    assertMatrixEquals(A.getQ(), id2);
    assertMatrixEquals(B.getQ(), id2);
    assertMatrixEquals(C.getQ(), id2);

    // net reward = 2 (i.e. 1 from fav and 1 from finish)
    // updates A and B's Q matrices only
    EventResource.learnFromEvent(p, a4); // changes Q matrices
    double[] expectedA = {1.375, 0.0, 0.0, 1.0};
    assertMatrixEquals(A.getQ(), expectedA);
    double[] expectedB = {1.0, 1.25, 0.0, 1.0};
    assertMatrixEquals(B.getQ(), expectedB);
    // C matric remains the same
    assertMatrixEquals(C.getQ(), id2);

    // net reward = 0 (i.e. 1 from fav and -1 from skip)
    EventResource.learnFromEvent(p, a5); // changes Q matrices
    double[] expectedA2 = {1.375, 0.25, 0.0, 1.0};
    assertMatrixEquals(A.getQ(), expectedA2);
    assertMatrixEquals(B.getQ(), expectedB);
    assertMatrixEquals(C.getQ(), id2);
  }

  private void assertMatrixEquals(List<List<Double>> Q, double[] expected) {
    for(int i = 0; i < Q.size(); i++) {
      for(int j = 0; j < Q.size(); j++) {
        assertEquals(expected[Q.size() * i + j], Q.get(i).get(j));
      }
    }
  }

  private static NodeCluster createTestHierarchy() {

    NodeCluster A = new NodeCluster();
    A.setId("A");
    A.setLevel(0);
    NodeCluster B = new NodeCluster();
    B.setId("B");
    B.setLevel(1);
    NodeCluster C = new NodeCluster();
    C.setId("C");
    C.setLevel(2);


    LeafCluster l1 = new LeafCluster();
    l1.setLevel(1);
    l1.setId("37-l1");
    l1.setSongId("l1");
    LeafCluster l2 = new LeafCluster();
    l2.setLevel(2);
    l2.setId("37-l2");
    l2.setSongId("l2");
    LeafCluster l3 = new LeafCluster();
    l3.setLevel(3);
    l3.setId("37-l3");
    l3.setSongId("l3");
    LeafCluster l4 = new LeafCluster();
    l4.setLevel(3);
    l4.setId("37-l4");
    l4.setSongId("l4");


    A.addChild(B); B.setParent(Key.create(NodeCluster.class, A.getId()));
    A.addChild(l1); l1.setParent(Key.create(NodeCluster.class, A.getId()));
    A.addSongId("l1"); A.addSongId("l2"); A.addSongId("l3"); A.addSongId("l4");

    B.addChild(C); C.setParent(Key.create(NodeCluster.class, B.getId()));
    B.addChild(l2); l2.setParent(Key.create(NodeCluster.class, B.getId()));
    B.addSongId("l2"); B.addSongId("l3"); B.addSongId("l4");

    C.addChild(l3); l3.setParent(Key.create(NodeCluster.class, C.getId()));
    C.addChild(l4); l4.setParent(Key.create(NodeCluster.class, C.getId()));
    C.addSongId("l3"); C.addSongId("l4");

    assertNotNull(l1.getParent());
    assertNotNull(l2.getParent());
    assertNotNull(l3.getParent());
    assertNotNull(l4.getParent());

    ofy().save().entities(A, B, C).now();
    ofy().save().entities(l1, l2, l3, l4).now();

    return A;
  }
}