package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class RequestManagerTest {
  RequestManager rm;
  WorkerManager wm;
  InventoryManager im;

  @Before public void setUp() {
    wm = new WorkerManager();
    rm = new RequestManager(wm);
  }
  
  @Test
  public void testAddRequest() {
    PickingRequest req = new PickingRequest(1);
    int initialPickingLength = rm.getPickingQueue().size();
    int initialLoadingLength = rm.getLoadingOrder().size();
    rm.addRequest(req);
    assertEquals(initialPickingLength + 1, rm.getPickingQueue().size());
    assertEquals(initialLoadingLength + 1, rm.getLoadingOrder().size());
  }

  @Test
  public void testSendToPickingNoQueuedPickers() {
    assertNull(wm.nextPicker());
    PickingRequest req = new PickingRequest(1);
    int initPickingLength = rm.getPickingQueue().size();
    rm.sendToPicking(req);
    int finalPickingLength = rm.getPickingQueue().size();
    assertEquals(initPickingLength + 1, finalPickingLength);   
  }
  
  @Test
  public void testSendToPickingWithQueuedPickers() {
    Picker p = new Picker("picker", rm, im);
    p.isAvail = true;
    rm.queueWorker(p);
    PickingRequest req = new PickingRequest(1);
    int initPickingLength = rm.getPickingQueue().size();
    rm.sendToPicking(req);
    int finalPickingLength = rm.getPickingQueue().size();
    assertEquals(initPickingLength, finalPickingLength);   
  }

  @Test
  public void testSendToSequencing() {
    assertEquals(0, rm.getSequencingQueue().size());
    PickingRequest req = new PickingRequest(1);
    rm.sendToSequencing(req);
    assertEquals(1, rm.getSequencingQueue().size());
  }

  @Test
  public void testSendToLoading() {
    assertEquals(0, rm.getLoadingQueue().size());
    PickingRequest req = new PickingRequest(1);
    rm.sendToLoading(req);
    assertEquals(1, rm.getLoadingQueue().size());
  }

  @Test
  public void testSendToReplenish() {
    assertEquals(0, rm.getReplenishQueue().size());
    rm.sendToReplenish("req");
    assertEquals(1, rm.getReplenishQueue().size());
  }

  @Test
  public void testGetPickingRequest() {
    assertEquals(0, rm.getPickingQueue().size());
    PickingRequest req = new PickingRequest(1);
    rm.sendToPicking(req);
    assertEquals(1, rm.getPickingQueue().size());
    rm.getPickingRequest();
    assertEquals(0, rm.getPickingQueue().size());
  }

  @Test
  public void testGetSequencingRequest() {
    assertEquals(0, rm.getSequencingQueue().size());
    PickingRequest req = new PickingRequest(1);
    rm.sendToSequencing(req);
    assertEquals(1, rm.getSequencingQueue().size());
    rm.getSequencingRequest();
    assertEquals(0, rm.getSequencingQueue().size());
  }

  @Test
  public void testGetLoadingRequest() {
    assertEquals(0, rm.getLoadingQueue().size());
    PickingRequest req = new PickingRequest(1);
    rm.sendToLoading(req);
    assertEquals(1, rm.getLoadingQueue().size());
    rm.getLoadingRequest();
    assertEquals(0, rm.getLoadingQueue().size());
  }

  @Test
  public void testGetReplenishRequest() {
    assertEquals(0, rm.getReplenishQueue().size());
    rm.sendToReplenish("req");
    assertEquals(1, rm.getReplenishQueue().size());
    rm.getReplenishingRequest();
    assertEquals(0, rm.getReplenishQueue().size());
  }

  @Test
  public void testReadyToLoadNoNextOrder() {
    assertFalse(rm.readyToLoad()); // 
  }

  @Test
  public void testReadyToLoadNextRequestToLoadNotInLoadingQueue() {
    PickingRequest req1 = new PickingRequest(1);
    rm.addRequest(req1); // req1 first in LoadingOrder
    rm.sendToSequencing(rm.getPickingRequest()); // req1 in SequencingQueue
    
    PickingRequest req2 = new PickingRequest(2);
    rm.addRequest(req2); // req2 second in LoadingOrder
    rm.sendToLoading(rm.getPickingRequest()); // req2 in LoadingQueue
    
    assertFalse(rm.readyToLoad()); // req2 cannot be loaded before req1
  }
  
  @Test
  public void testReadyToLoadNextRequestToLoadInLoadingQueue() {
    PickingRequest req1 = new PickingRequest(1);
    rm.addRequest(req1); // req1 first in LoadingOrder
    rm.sendToLoading(rm.getPickingRequest()); // req1 moved to LoadingQueue
    assertTrue(rm.readyToLoad()); // req1 is ready to be loaded
  }

  @Test
  public void testCompleteRequest() {
    //
    Order o1 = new Order("Red", "S");
    Order[] orderArray = {o1, o1, o1, o1};
    ArrayList<Order> orderList = new ArrayList<>(Arrays.asList(orderArray));
    
    String[] skuArray = {"front", "front", "front", "front", "rear", "rear", "rear", "rear"};
    ArrayList<String> skuList = new ArrayList<>(Arrays.asList(skuArray));
    
    PickingRequest pr1 = new PickingRequest(orderList, skuList);
    PickingRequest pr2 = new PickingRequest(orderList, skuList);

    rm.addRequest(pr1);
    rm.addRequest(pr2);
    assertEquals(pr1, rm.getPickingQueue().peek()); // pr1 is in PickingQueue
    
    rm.sendToLoading(pr2);
    assertEquals(pr2, rm.getLoadingQueue().peek()); // pr2 is in LoadingQueue
    
    rm.completeRequest(pr2);
    assertEquals(2, rm.getLoadingOrder().size()); // pr2 could not be loaded
    
    rm.sendToLoading(pr1);
    rm.completeRequest(pr1);
    assertEquals(1, rm.getLoadingOrder().size()); // pr1 was loaded
    
    rm.completeRequest(pr2);
    assertEquals(0, rm.getLoadingOrder().size()); // pr2 was loaded
  }

  @Test
  public void testQueuePicker() {
    assertNull(wm.nextPicker());
    rm.queueWorker(new Picker("Penny", rm, im));
    assertNotNull(wm.nextPicker()); // the new Picker was added to the queue
  }
  
  @Test
  public void testQueueSequencer() {
    assertNull(wm.nextSequencer());
    rm.queueWorker(new Sequencer("Steve", rm, im));
    assertNotNull(wm.nextSequencer()); // the new Sequencer was added to the queue
  }
  
  @Test
  public void testQueueLoader() {
    assertNull(wm.nextLoader());
    rm.queueWorker(new Loader("Pete", rm, im));
    assertNotNull(wm.nextLoader());  // the new Loader was added to the queue
  }
  
  @Test
  public void testQueueReplenisher() {
    assertNull(wm.nextReplenisher());
    rm.queueWorker(new Replenisher("Rita", rm, im));
    assertNotNull(wm.nextReplenisher());  // the new Replenisher was added to the queue
  }
}
