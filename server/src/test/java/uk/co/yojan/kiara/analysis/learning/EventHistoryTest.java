package uk.co.yojan.kiara.analysis.learning;

import junit.framework.TestCase;

import java.util.LinkedList;

public class EventHistoryTest extends TestCase {

  // addEnd(LinkedList<String> eventHistory, String previousSongId, String endedSongId)

  LinkedList<String> eventHistory;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    eventHistory = new LinkedList<>();
  }

  public void testAddEnd() throws Exception {
    EventHistory.addEnd(eventHistory, "A", "B");
    assertEquals(1, eventHistory.size());
    assertEquals("A-B-END", eventHistory.getFirst());

    EventHistory.addEnd(eventHistory, "C", "D");
    assertEquals(2, eventHistory.size());
    assertEquals("A-B-END", eventHistory.getFirst());
    assertEquals("C-D-END", eventHistory.getLast());

    // Check Sliding Window procedure works.
    EventHistory.setEventHistorySize(5);
    for(int i = 0; i < 5; i++) EventHistory.addEnd(eventHistory, "A"+i, "B"+i);
    assertEquals(5, eventHistory.size());
    assertEquals("A0-B0-END", eventHistory.getFirst());
    assertEquals("A4-B4-END", eventHistory.getLast());
  }

  public void testAddSkipped() throws Exception {
    EventHistory.addSkipped(eventHistory, "A", "B", 0);
    assertEquals(1, eventHistory.size());
    assertEquals("A-B-SKIP-0.0", eventHistory.getFirst());

    EventHistory.addSkipped(eventHistory, "C", "D", 1);
    assertEquals(2, eventHistory.size());
    assertEquals("A-B-SKIP-0.0", eventHistory.getFirst());
    assertEquals("C-D-SKIP-1.0", eventHistory.getLast());

    // Check Sliding Window procedure works.
    EventHistory.setEventHistorySize(5);
    for(int i = 0; i < 5; i++) EventHistory.addSkipped(eventHistory, "A"+i, "B"+i, i);
    assertEquals(5, eventHistory.size());
    assertEquals("A0-B0-SKIP-0.0", eventHistory.getFirst());
    assertEquals("A4-B4-SKIP-4.0", eventHistory.getLast());
  }
}