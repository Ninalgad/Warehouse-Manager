package project;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;


public class WorkerManagerTest {
  WorkerManager wm;
  RequestManager rm;
  InventoryManager im;
  
  @Before public void setUp() {
    wm = new WorkerManager();
  }
  
  @Test
  public void testEnqueuePicker() {
    Picker picker = new Picker("p", rm, im);
    assertNull(wm.nextPicker());
    wm.enqueueWorker(picker);
    assertNotNull(wm.nextPicker());
  }
  
  @Test
  public void testEnqueueSequencer() {
    Sequencer sequencer = new Sequencer("s", rm, im);
    assertNull(wm.nextSequencer());
    wm.enqueueWorker(sequencer);
    assertNotNull(wm.nextSequencer());
  }
  
  @Test
  public void testEnqueueLoader() {
    Loader loader = new Loader("l", rm, im);
    assertNull(wm.nextLoader());
    wm.enqueueWorker(loader);
    assertNotNull(wm.nextLoader());
  }

  @Test
  public void testEnqueueReplenisher() {
    Replenisher replenisher = new Replenisher("r", rm, im);
    assertNull(wm.nextReplenisher());
    wm.enqueueWorker(replenisher);
    assertNotNull(wm.nextReplenisher());
  }
  
  @Test
  public void testUnrecognizedWorker() {
    // create a dummy class that extends Worker
    final class UnrecognizedWorker extends Worker {
      public UnrecognizedWorker(String id, RequestManager rm, InventoryManager im) {
        super(id, rm, im);        
      }
      
      public void process() {}
      
      public Object getRequest() {
        return new Object();
      }
      
      public void sendNextRequest() {}
    } 
    
    // ensure all queues are empty
    assertNull(wm.nextPicker());
    assertNull(wm.nextSequencer());
    assertNull(wm.nextLoader());
    assertNull(wm.nextReplenisher());
    
    // check that the UnrecognizedWorker can't be added to any queues
    UnrecognizedWorker w = new UnrecognizedWorker("test", rm, im);
    wm.enqueueWorker(w);
    assertNull(wm.nextPicker());
    assertNull(wm.nextSequencer());
    assertNull(wm.nextLoader());
    assertNull(wm.nextReplenisher());
  }


}
