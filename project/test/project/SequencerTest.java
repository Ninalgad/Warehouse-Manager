package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class SequencerTest {
  private InventoryManager im;
  private RequestManager rm;
  private PickingRequest pr1;
  private PickingRequest pr2;
  private Sequencer s1;
  private Loader l1;
  private ArrayList<String> skus1;
  private ArrayList<String> skus2;

  @Test
  public final void testSendNextRequest() {
    rm.sendToSequencing(pr1);

    s1.doNextTask();

    s1.sendNextRequest();

    assertTrue(s1.getIsAvail()); // check if finishTask() and clearValidFascias() is called
    assertEquals(null, s1.getCurrReq());
    assertFalse(s1.getValidFascias().contains(true));
    assertFalse(s1.getValidFascias().contains(false));

    l1.doNextTask();
    assertEquals(pr1, l1.getCurrReq());
    assertEquals(skus1, l1.getCurrReq().getFasciaState()); // check if fascias are rearranged
  }

  @Test
  public final void testGetRequest() {
    rm.sendToSequencing(pr1);

    assertNull(s1.getCurrReq());
    s1.doNextTask();
    assertEquals(pr1, s1.getCurrReq());
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

    s1 = new Sequencer("s", rm, im);
    l1 = new Loader("l", rm, im);

    pr1 = new PickingRequest(1) {
      @Override
      public ArrayList<String> getLoadingOrder() {
        return skus1;
      }
    };
    
    skus1 = new ArrayList<String>();
    skus2 = new ArrayList<String>();
    for (Integer i = 0; i < 8; i++) {
      skus1.add(i.toString());
      skus2.add(String.valueOf(7 - i));
      pr1.updateFasciaState(skus1.get(i));
    }
  }
}
