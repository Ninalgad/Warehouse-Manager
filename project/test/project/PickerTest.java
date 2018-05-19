package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class PickerTest {
  private InventoryManager im;
  private RequestManager rm;
  private PickingRequest pr1;
  private PickingRequest pr2;
  private ArrayList<String> skus1;
  private ArrayList<String> skus2;
  private Picker p1;

  @Test
  public final void testPickWithoutTask() {
    assertTrue(p1.currIndex.equals(0));
    p1.pick("0");
    assertTrue(p1.currIndex.equals(0)); // doesn't change current index
  }

  @Test
  public final void testPickWrongFascias() {
    rm.sendToPicking(pr1);
    p1.doNextTask();

    p1.pick("0");
    p1.pick("1");
    p1.pick("2");
    assertTrue(p1.currIndex.equals(3));

    p1.pick("4");
    p1.pick("4");
    p1.pick("5");
    p1.pick("7");
    assertTrue(p1.currIndex.equals(3)); // doesn't change current index picking wrong fascia
  }

  @Test
  public final void testPickRightFascias() {
    rm.sendToPicking(pr1);
    p1.doNextTask();
    p1.pick("0");
    p1.pick("1");
    p1.pick("2");
    p1.pick("3");
    p1.pick("4");
    p1.pick("5");
    p1.pick("6");
    p1.pick("7");
    assertTrue(p1.currIndex.equals(8)); // current index picking correct fascias change
  }

  @Test
  public final void testPickOverPick() {
    rm.sendToPicking(pr1);
    p1.doNextTask();
    p1.pick("0");
    p1.pick("1");
    p1.pick("2");
    p1.pick("3");
    p1.pick("4");
    p1.pick("5");
    p1.pick("6");
    p1.pick("7");
    assertTrue(p1.currIndex.equals(8));
    p1.pick("3");
    assertTrue(p1.currIndex.equals(8)); // doesn't change current index after finished picking
  }

  @Test
  public final void testGetRequest() {
    rm.sendToPicking(pr1);

    assertNull(p1.getCurrReq());
    p1.doNextTask();
    assertEquals(pr1, p1.getCurrReq());
  }

  @Test
  public final void testSendNextRequestBeforeFinish() {
    rm.sendToPicking(pr1);
    p1.doNextTask();

    p1.pick("0");
    p1.pick("1");
    p1.pick("2");
    p1.pick("3");
    p1.pick("4");
    p1.pick("5");
    p1.pick("6");
    assertEquals(pr1, p1.getCurrReq());
    assertTrue(p1.getCurrIndex().equals(7));
    p1.sendNextRequest();

    assertEquals(skus1, p1.getPickingOrder()); // sendNextRequest() and finishTask() denied
  }

  @Test
  public final void testSendNextRequestAfterFinish() {
    rm.sendToPicking(pr1);
    p1.doNextTask();

    p1.pick("0");
    p1.pick("1");
    p1.pick("2");
    p1.pick("3");
    p1.pick("4");
    p1.pick("5");
    p1.pick("6");
    p1.pick("7");
    p1.sendNextRequest();
    assertNull(p1.getPickingOrder());
    assertTrue(p1.getCurrIndex().equals(0));
    assertTrue(p1.getIsAvail());
  }

  @Test
  public final void testSendNextRequestWithoutTask() {
    p1.currIndex = 2;
    p1.sendNextRequest(); // finishTask() and variables will not be reset in this case
    assertTrue(p1.getCurrIndex().equals(2));
  }

  @Test
  public final void testProcess() {
    rm.sendToPicking(pr1);
    assertNull(p1.getPickingOrder());
    p1.doNextTask();
    assertEquals(skus1, p1.getPickingOrder());
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
        public void decrement(String sku) {
          return;
        }
      };
    } catch (FileNotFoundException ex) {
      System.out.println("couldn't open a specified file in IM during testing");
      ex.printStackTrace();
    }
    p1 = new Picker("p", rm, im);

    skus1 = new ArrayList<String>();
    skus2 = new ArrayList<String>();
    for (Integer i = 0; i < 8; i++) {
      skus1.add(i.toString());
      skus2.add(String.valueOf(7 - i));
    }

    pr1 = new PickingRequest(1) {
      @Override
      public ArrayList<String> getPickingOrder() {
        return skus1;
      }

      @Override
      public void updateFasciaState(String sku) {
        return;
      }
    };
  }
}
