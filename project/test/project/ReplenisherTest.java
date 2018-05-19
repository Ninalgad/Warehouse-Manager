package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

public class ReplenisherTest {
  private InventoryManager im;
  private RequestManager rm;
  private String pr1;
  private String pr2;
  private Replenisher r1;

  @Test
  public final void testReplenishWithoutTask() {
    r1.currIndex = 1;
    r1.isAvail = false;
    r1.replenish(); // finishTask() is not called in this case
    assertTrue(r1.getCurrIndex().equals(1));
    assertFalse(r1.getIsAvail());
  }
  
  @Test
  public final void testReplenishDenied() {
    rm.sendToReplenish(pr2);
    r1.doNextTask();

    r1.replenish(); // calls finishTask() regardless of success or deny

    assertTrue(r1.getIsAvail());
    assertEquals("", r1.getReplenisherReq());
  }
  
  @Test
  public final void testReplenishAccepted() {
    rm.sendToReplenish(pr1);
    r1.doNextTask();
    assertTrue(!r1.getIsAvail());
    assertEquals(pr1, r1.getReplenisherReq());
    r1.replenish(); // also calls finishTask()
    assertTrue(r1.getIsAvail());
    assertEquals("", r1.getReplenisherReq());
  }
  
  @Test
  public final void testDoNextTaskNoTask() {
    assertEquals(r1.getReplenisherReq(), "");
    r1.doNextTask();
    assertEquals(r1.getReplenisherReq(), "");
  }
  
  @Test
  public final void testDoNextTaskYesTaskAvail() {
    rm.sendToReplenish(pr1);
    r1.doNextTask();
    assertEquals(r1.getReplenisherReq(), pr1);
  }
  
  @Test
  public final void testDoNextTaskYesTaskUnavail() {
    rm.sendToReplenish(pr2);
    r1.doNextTask();
    assertEquals(r1.getReplenisherReq(), pr1);
  }

  /**
   * Sets up the things that is always used before running each test.
   */
  @Before
  public void setupMan() throws FileNotFoundException {
    rm = new RequestManager(new WorkerManager());

    try {
      im = new InventoryManager("initial.csv", rm, false) {
        @Override
        public Boolean replenish(String loc) {
          return loc.equals(pr1);
        }
      };
    } catch (FileNotFoundException ex) {
      System.out.println("couldn't open a specified file in IM during testing");
      ex.printStackTrace();
    }

    r1 = new Replenisher("r", rm, im);
    pr1 = "A,1,2,0";
    pr2 = "B,0,0,3";
  }
}
