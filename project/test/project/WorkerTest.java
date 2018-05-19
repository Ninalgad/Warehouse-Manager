package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

public class WorkerTest { // since
  private InventoryManager im;
  private RequestManager rm;
  private PickingRequest pr1;
  private PickingRequest pr2;
  private String pr3;
  private Picker p1;
  private Replenisher r1;
  private Sequencer s1;
  private Loader l1;

  @Test
  public final void testDoNextTaskWithoutTask() {
    assertNull(p1.getCurrReq());
    p1.doNextTask();
    assertNull(p1.getCurrReq());
  }
  
  @Test
  public final void testDoNextTaskAvailWithTask() {
    rm.sendToPicking(pr1);
    p1.doNextTask();
    assertEquals(pr1, p1.getCurrReq());
  }
  
  @Test
  public final void testDoNextTaskUnavailWithTask() {
    rm.sendToPicking(pr1);
    p1.doNextTask();
    rm.sendToPicking(pr2);
    p1.doNextTask();
    assertEquals(pr1, p1.getCurrReq());
  }

  @Test
  public final void testFinishTask() {
    r1.isAvail = false;
    r1.currIndex = 1;
    r1.currReq = pr1;
    r1.replenisherReq = "sth";

    r1.finishTask();
    assertTrue(r1.getIsAvail());
    assertNull(r1.getCurrReq());
    assertTrue(r1.getCurrIndex().equals(0));
    assertEquals("", r1.getReplenisherReq());
  }

  @Test
  public final void testGetType() {
    assertEquals("Picker", p1.getType());
    assertEquals("Loader", l1.getType());
    assertEquals("Sequencer", s1.getType());
    assertEquals("Replenisher", r1.getType());
  }

  @Test
  public final void testIsAvail() {
    assertTrue(p1.getIsAvail());
    p1.isAvail = false;
    assertFalse(p1.getIsAvail());
  }

  @Test
  public final void testWorker() {
    assertEquals("Picker p1", p1.toString());
    p1.isAvail = false;
    p1.currReq = pr1;
    assertEquals("Picker p1 working on PickingRequest #1", p1.toString());

    assertEquals("Replenisher r1", r1.toString());
    r1.isAvail = false;
    r1.replenisherReq = pr3;
    assertEquals("Replenisher r1 replenishing location: " + pr3, r1.toString());
  }


  /**
   * Sets up the things that is always used before running each test.
   */
  @Before
  public void setupMan() throws FileNotFoundException {
    rm = new RequestManager(new WorkerManager());

    try {
      im = new InventoryManager("initial.csv", rm, false);
    } catch (FileNotFoundException ex) {
      System.out.println("couldn't open a specified file in IM during testing");
      ex.printStackTrace();
    }
    p1 = new Picker("p1", rm, im);
    r1 = new Replenisher("r1", rm, im);
    l1 = new Loader("l1", rm, im);
    s1 = new Sequencer("s1", rm, im);

    pr1 = new PickingRequest(1);
    pr2 = new PickingRequest(2);
    pr3 = "A,0,2,1";
  }
}
