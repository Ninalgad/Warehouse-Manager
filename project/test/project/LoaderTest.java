package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class LoaderTest {

  private InventoryManager im;
  private RequestManager rm;
  private PickingRequest pr1;
  private PickingRequest pr2;
  private Loader l1;
  private ArrayList<String> skus1;
  private ArrayList<String> skus2;

  @Test
  public final void testSendNextRequestWithTask() {
    rm.addRequest(pr1);
    rm.sendToLoading(pr1);
 
    l1.doNextTask();
    l1.sendNextRequest();
    
    assertFalse(rm.getLoadingOrder().contains(pr1)); // pr1 is completed; removed from list
    
    assertTrue(l1.getIsAvail()); // check if finishTask() and clearValidFascias() is called
    assertEquals(null, l1.getCurrReq());
    assertFalse(l1.getValidFascias().contains(true));
    assertFalse(l1.getValidFascias().contains(false));
  }
  
  @Test
  public final void testSendNextRequestWithoutTask() {
    assertEquals(null, l1.getCurrReq());
    
    l1.isAvail = false;
    l1.currIndex = 4;
    
    l1.sendNextRequest();
    
    assertFalse(l1.getIsAvail()); // check if finishTask() and clearValidFascias() is called
    assertEquals(null, l1.getCurrReq());
    assertFalse(l1.getCurrIndex().equals(0));
  }

  @Test
  public final void testGetRequest() {
    rm.addRequest(pr1);
    rm.sendToLoading(pr1);

    assertNull(l1.getCurrReq());
    l1.doNextTask();
    assertEquals(pr1, l1.getCurrReq());
  }

  /**
   * Sets up the things that is always used before running each test.
   */
  @Before
  public void setupMan() throws FileNotFoundException {
    rm = new RequestManager(new WorkerManager()) {
      @Override
      public void completeRequest(PickingRequest req) {
        PickingRequest nextInLine = this.getLoadingOrder().peek();
        if (req.equals(nextInLine)) {
          this.getLoadingOrder().pop(); // allow the next Order to be loaded
        } else {
          sendToLoading(req);
        }
      }
    };

    try {
      im = new InventoryManager("initial.csv", rm, false);
    } catch (FileNotFoundException ex) {
      System.out.println("couldn't open a specified file in IM during testing");
      ex.printStackTrace();
    }
    l1 = new Loader("l1", rm, im);

    pr1 = new PickingRequest(1) {
      @Override
      public ArrayList<String> getLoadingOrder() {
        return skus1;
      }
    };
    pr2 = new PickingRequest(2) {
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
      pr2.updateFasciaState(skus2.get(i));
    }
  }
}
