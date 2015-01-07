package uk.co.yojan.kiara.analysis.learning;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import uk.co.yojan.kiara.analysis.OfyUtils;
import uk.co.yojan.kiara.analysis.cluster.LeafCluster;
import uk.co.yojan.kiara.analysis.cluster.NodeCluster;

import java.util.ArrayList;
import java.util.List;

import static uk.co.yojan.kiara.server.OfyService.ofy;

public class QLearnerTest extends TestCase {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  LeafCluster l1, l2, l3, l4;
  NodeCluster A, B, C;

  /**
   *
   * The Hierarchical cluster structure to be tested:
   *
   * Key:
   *   ( X )   represents a NodeCluster with id X.
   *   (( l )) represents a LeafCluster with id l.
   *
   *                    TREE            LEVEL
   *
   *                    ( A )             0
   *                   /     \
   *              ( B )      (( l1 ))     1
   *              /   \
   *          ( C )    (( l2 ))           2
   *          /   \
   *         /     \
   *  (( l3 ))     (( l4 ))               3
   *
   */

  @Before
  public void setUp() {
    helper.setUp();

    A = createTestHierarchy();
    B = OfyUtils.loadNodeCluster("B").now();
    C = OfyUtils.loadNodeCluster("C").now();

    l1 = ofy().load().key(Key.create(LeafCluster.class, "l1")).now();
    l2 = ofy().load().key(Key.create(LeafCluster.class, "l2")).now();
    l3 = ofy().load().key(Key.create(LeafCluster.class, "l3")).now();
    l4 = ofy().load().key(Key.create(LeafCluster.class, "l4")).now();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  public void testUpdateQMatrixUnitReward() throws Exception {

      // 3x3 identity matrix
      List<List<Double>> Q = identity(3);


      /** Tests for reward = alpha = gamma = 1 **/
      // state 0, action 0, reward 1, alpha 1, gamma 1
      // expect Q[0][0] to be 2 according to Q relation
      QLearner.updateQMatrix(Q, 0, 0, 1, 1, 1);
      assertEquals(2.0, Q.get(0).get(0));

      // state 0, action 1, reward 1, alpha 1, gamma 1
      QLearner.updateQMatrix(Q, 0, 1, 1, 1, 1);
      assertEquals(2.0, Q.get(0).get(1));

      QLearner.updateQMatrix(Q, 1, 0, 1, 1, 1);
      assertEquals(3.0, Q.get(1).get(0));

      // Q[2][1] = (1 - 1)Q[2][1] + 1 * (1 + 1 * 3.0) = 4.0
      QLearner.updateQMatrix(Q, 2, 1, 1, 1, 1);

      double[] expectedQ = {2.0, 2.0, 0.0,
                            3.0, 1.0, 0.0,
                            0.0, 4.0, 1.0};

      /** Tests for fractional reward, alpha and gamma. **/
      // reset Q
      Q = identity(3);

      // reward = 2, alpha = 0.5, gamma = 0.25
      QLearner.updateQMatrix(Q, 0, 0, 2, 0.5, 0.25);
      QLearner.updateQMatrix(Q, 0, 1, 2, 0.5, 0.25);
      QLearner.updateQMatrix(Q, 1, 0, 2, 0.5, 0.25);
      QLearner.updateQMatrix(Q, 2, 1, 2, 0.5, 0.25);

      double[] expectedQprime = {1.625,    1.125,       0.0,
                                 1.203125, 1.0,         0.0,
                                 0.0,      1.150390625, 1.0};

      for(int i = 0; i < Q.size(); i++) {
        for(int j = 0; j < Q.size(); j++) {
          assertEquals(expectedQprime[Q.size() * i + j], Q.get(i).get(j));
        }
      }
    }

  public void testLearn() throws Exception {
    // creates the hierarchy as displayed above
    // test for a unit reward

    /** (l3, l4, 1.0) -- leaves at same level. affects all NodeClusters. **/
    // Qc(0, 1) = 0.5 * 0 + 0.5(1 + 0.5 * 1) = 0.75
    // Qb(0, 0) = 0.75 * 1 + 0.25(1 + 0.5 * 1) = 1.125
    // Qa(0, 0) = 0.875 * 1 + 0.125(1 + 0.5 * 1) = 1.0625
    QLearner.update(l3, l4, 1.0);

    double[] expectedQa = {1.0625, 0.0, 0.0, 1.0};
    double[] expectedQb = {1.125, 0.0, 0.0, 1.0};
    double[] expectedQc = {1.0, 0.75, 0.0, 1.0};

    assertMatrixEquals(A.getQ(), expectedQa);
    assertMatrixEquals(B.getQ(), expectedQb);
    assertMatrixEquals(C.getQ(), expectedQc);

    /** (l3, l2, 1.0) -- leaves at different levels. affects NodeClusters A and B. **/
    // Qc remains the same
    // Qb(0, 1) = 0.5 * 0 + 0.5(1 + 0.5 * 1) = 0.75
    // Qa(0, 0) = 0.75 * 1.0625 + 0.25(1 + 0.5 * 1.0625) = 1.1796875
    QLearner.update(l3, l2, 1.0);

    double[] expectedQa2 = {1.1796875, 0.0, 0.0, 1.0};
    double[] expectedQb2 = {1.125, 0.75, 0.0, 1.0};

    assertMatrixEquals(A.getQ(), expectedQa2);
    assertMatrixEquals(B.getQ(), expectedQb2);
    assertMatrixEquals(C.getQ(), expectedQc); // same as previously used

    /** (l1, l4, 1.0) -- leaves that only affect the root node, A. **/
    // Qc remains the same
    // Qb remains the same
    // Qa(1, 0) = 0.5 * 0 + 0.5(1 + 0.5 * 1.1796875) = 0.794921875
    QLearner.update(l1, l4, 1.0);

    double[] expectedQa3 = {1.1796875, 0.0, 0.794921875, 1.0};

    assertMatrixEquals(A.getQ(), expectedQa3);
    assertMatrixEquals(B.getQ(), expectedQb2);
    assertMatrixEquals(C.getQ(), expectedQc);
  }

  public void testLearn2() throws Exception {
    QLearner.update(l4, l2, 2.0);
    NodeCluster B3 = OfyUtils.loadNodeCluster("B").now();
    double[] expected = {1.0, 1.25, 0.0, 1.0};
    assertMatrixEquals(B3.getQ(), expected);
  }

  private void assertMatrixEquals(List<List<Double>> Q, double[] expected) {
    for(int i = 0; i < Q.size(); i++) {
      for(int j = 0; j < Q.size(); j++) {
        assertEquals(expected[Q.size() * i + j], Q.get(i).get(j));
      }
    }
  }

  public void testCommonAncestor() throws Exception {
    NodeCluster root = createTestHierarchy();
    assertTrue(QLearner.commonAncestor(root, "l1", "l2"));
    assertTrue(QLearner.commonAncestor(root, "l2", "l3"));
    assertTrue(QLearner.commonAncestor(root, "l3", "l4"));
    assertTrue(QLearner.commonAncestor(root, "l1", "l4"));

    NodeCluster B = (NodeCluster) root.getChildren().get(0);
    assertTrue(QLearner.commonAncestor(B, "l2", "l3"));
    assertTrue(QLearner.commonAncestor(B, "l2", "l4"));
    assertTrue(QLearner.commonAncestor(B, "l3", "l4"));
    assertFalse(QLearner.commonAncestor(B, "l1", "l2"));
    assertFalse(QLearner.commonAncestor(B, "l1", "l4"));

    NodeCluster C = (NodeCluster) B.getChildren().get(0);
    assertTrue(QLearner.commonAncestor(C, "l3", "l4"));
    assertFalse(QLearner.commonAncestor(C, "l1", "l3"));
    assertFalse(QLearner.commonAncestor(C, "l1", "l4"));
    assertFalse(QLearner.commonAncestor(C, "l2", "l1"));
  }

  private List<List<Double>> identity(int dim) {
    List<List<Double>> id = new ArrayList<>();
    for(int i = 0; i < dim; i++) {
      ArrayList<Double> row = new ArrayList<>();
      for(int j = 0; j < dim; j++) {
        row.add(0.0);
      }
      row.set(i, 1.0);
      id.add(row);
    }
    return id;
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
    l1.setId("l1");
    l1.setSongId("l1");
    LeafCluster l2 = new LeafCluster();
    l2.setLevel(2);
    l2.setId("l2");
    l2.setSongId("l2");
    LeafCluster l3 = new LeafCluster();
    l3.setLevel(3);
    l3.setId("l3");
    l3.setSongId("l3");
    LeafCluster l4 = new LeafCluster();
    l4.setLevel(3);
    l4.setId("l4");
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

    ofy().save().entities(A, B, C).now();
    ofy().save().entities(l1, l2, l3, l4).now();

    return A;
  }
}