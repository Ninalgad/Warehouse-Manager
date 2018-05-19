package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CheckerWorkerTest {
  private InventoryManager im;
  private RequestManager rm;
  private PickingRequest pr1;
  private PickingRequest pr2;
  private ArrayList<String> skus1;
  private ArrayList<String> skus2;
  private Sequencer s1;
  private Loader l1;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @Test
  public final void testCheckerWorker() {
    assertEquals("Loader l1", l1.toString());
    l1.isAvail = false;
    l1.currReq = pr1;
    assertEquals("Loader l1 working on PickingRequest #1", l1.toString());
    assertFalse(l1.getValidFascias().contains(true));
    assertFalse(l1.getValidFascias().contains(false));
  }

  @Test
  public final void testProcess() {
    rm.sendToSequencing(pr1);
    s1.doNextTask();
    assertEquals(skus1, s1.getExpectedFascias());
    assertEquals(skus2, s1.getGivenFascias());
  }

  @Test
  public final void testCheckWithoutTask() {
    assertTrue(s1.getCurrIndex().equals(0));
    s1.check();
    assertTrue(s1.getCurrIndex().equals(0));

    assertTrue(s1.getCurrIndex().equals(0));
    assertFalse(s1.getValidFascias().contains(true));
    assertFalse(s1.getValidFascias().contains(false));
  }

  @Test
  public final void testCheckAllValid() {
    rm.sendToSequencing(pr1);
    s1.doNextTask();

    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    assertTrue(s1.getCurrIndex().equals(8));
    assertFalse(s1.getValidFascias().contains(false));
    assertFalse(s1.getValidFascias().contains(null));
  }
  
  @Test
  public final void testCheckAlreadyWrong() {
    rm.sendToSequencing(pr1);
    s1.doNextTask();
    
    skus1.set(1, "10");
    s1.check();
    assertFalse(s1.getValidFascias().contains(false));
    s1.check();
    assertTrue(s1.getValidFascias().contains(false));
    assertTrue(s1.getCurrIndex().equals(2));

    s1.check();
    assertTrue(s1.getCurrIndex().equals(2));
  }
  
  @Test
  public final void testCheckOverCheck() {
    rm.sendToSequencing(pr1);
    s1.doNextTask();
   
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    s1.check();
    assertTrue(s1.getCurrIndex().equals(8));
    s1.check();
    assertTrue(s1.getCurrIndex().equals(8));
  }
  
  @Test
  public final void testRescan() {
    String temp;

    temp = skus1.get(1);
    skus1.set(1, "10");

    rm.sendToSequencing(pr1);
    s1.doNextTask();

    assertTrue(s1.getCurrIndex().equals(0));
    assertFalse(s1.getValidFascias().contains(true));
    s1.check();
    s1.check();
    assertTrue(s1.getCurrIndex().equals(2));
    assertTrue(s1.getValidFascias().contains(true));
    assertTrue(s1.getValidFascias().contains(false));

    s1.rescan();
    assertTrue(s1.getCurrIndex().equals(0));
    assertFalse(s1.getValidFascias().contains(true));
    assertFalse(s1.getValidFascias().contains(false));

    skus1.set(1, temp);
  }

  @Test
  public final void testSendToRepick() {
    rm.sendToPicking(pr2);
    rm.sendToSequencing(pr1);
    
    s1.doNextTask();
    s1.sendToRepick(); // calls finishTask()
    assertTrue(s1.getIsAvail());
    assertEquals(null, s1.getCurrReq());
    assertFalse(s1.getValidFascias().contains(true));
    assertFalse(s1.getValidFascias().contains(false));

    Picker p1 = new Picker("p", rm, im); // since rm.PickingOrder prioritizes pr1 > pr2
    p1.doNextTask();
    assertEquals(p1.getCurrReq(), pr1); // shows that the pr1 has been sent by rm
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
    s1 = new Sequencer("s1", rm, im);
    l1 = new Loader("l1", rm, im);

    pr1 = new PickingRequest(1) {
      @Override
      public ArrayList<String> getLoadingOrder() {
        return skus1;
      }
    
      @Override
      public ArrayList<String> getFasciaState() {
        return skus2;
      }
    };
    pr2 = new PickingRequest(2) {
      @Override
      public ArrayList<String> getLoadingOrder() {
        return skus1;
      }
    
      @Override
      public ArrayList<String> getFasciaState() {
        return skus2;
      }
    };
    
    skus1 = new ArrayList<String>();
    skus2 = new ArrayList<String>();
    for (Integer i = 0; i < 8; i++) {
      skus1.add(i.toString());
      skus2.add(String.valueOf(7 - i));
    }
  }
}
