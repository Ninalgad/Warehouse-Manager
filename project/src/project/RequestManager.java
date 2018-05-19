package project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestManager {
  private PriorityQueue<PickingRequest> pickingQueue = new PriorityQueue<>();
  private ArrayDeque<PickingRequest> loadingOrder = new ArrayDeque<>();
  private ArrayDeque<PickingRequest> sequencingQueue = new ArrayDeque<>();
  private ArrayDeque<String> replenishQueue = new ArrayDeque<>();
  private PriorityQueue<PickingRequest> loadingQueue = new PriorityQueue<>();
  private WorkerManager wm;
  private static final Logger logger = LoggerFactory.getLogger(RequestManager.class);

  /**
   * Creates a new RequestManager using the provided WorkerManager.
   * 
   * @param wm - The WorkerManager to handle Worker queues.
   */
  public RequestManager(WorkerManager wm) {
    this.wm = wm;
  }

  PriorityQueue<PickingRequest> getPickingQueue() {
    return pickingQueue;
  }


  ArrayDeque<PickingRequest> getLoadingOrder() {
    return loadingOrder;
  }


  ArrayDeque<PickingRequest> getSequencingQueue() {
    return sequencingQueue;
  }


  ArrayDeque<String> getReplenishQueue() {
    return replenishQueue;
  }


  PriorityQueue<PickingRequest> getLoadingQueue() {
    return loadingQueue;
  }


  /**
   * Introduces a new <code>PickingRequest</code> into the queueing system by placing it in the
   * picking queue and updates the loading order.
   */
  void addRequest(PickingRequest req) {
    enqueueRequest(req, loadingOrder);
    this.sendToPicking(req);
  }

  /**
   * Enqueues a <code>PickingRequest</code> to the specified queue.
   */
  private void enqueueRequest(PickingRequest req, AbstractCollection<PickingRequest> queue) {
    queue.add(req);
  }

  /**
   * Sends a <code>PickingRequest</code> to the picking queue, then notifies the first available
   * <code>Picker</code> in the <code>Picker</code> queue (if not empty).
   * 
   * @param req - The request being moved to the picking queue.
   */
  void sendToPicking(PickingRequest req) {
    enqueueRequest(req, pickingQueue);
    logger.info(String.format("PickingRequest #%d added to Picking Queue", req.getId()));
    Picker nextPicker = wm.nextPicker();
    if (nextPicker != null) {
      nextPicker.doNextTask();
    }
  }

  /**
   * Sends a <code>PickingRequest</code> to the sequencing queue, then notifies the first available
   * <code>Sequencer</code> in the <code>Sequencer</code> queue (if not empty).
   * 
   * @param req - The request being moved to the sequencing queue.
   */
  void sendToSequencing(PickingRequest req) {
    enqueueRequest(req, sequencingQueue);
    logger.info(String.format("PickingRequest #%d added to Sequencing Queue", req.getId()));
    Sequencer nextSequencer = wm.nextSequencer();
    if (nextSequencer != null) {
      nextSequencer.doNextTask();
    }
  }

  /**
   * Sends a <code>PickingRequest</code> to the loading queue. If the loading queue contains the
   * next <code>PickingRequest</code> in the loading order, the next available <code>Loader</code>
   * in the <code>Loader</code> queue is notified.
   * 
   * @param req - The request being moved to the loading queue.
   */
  void sendToLoading(PickingRequest req) {
    enqueueRequest(req, loadingQueue);
    logger.info(String.format("PickingRequest #%d added to Sequencing Queue", req.getId()));

    Loader nextLoader = wm.nextLoader();
    if (nextLoader != null) {
      nextLoader.doNextTask();
    }

  }


  /**
   * Sends a replenishing request for an understocked fascia.
   * 
   * @param location - The <code>String</code> location of the understocked fascia.
   */
  void sendToReplenish(String location) {
    replenishQueue.add(location);
    Replenisher nextReplenisher = wm.nextReplenisher();
    if (nextReplenisher != null) {
      nextReplenisher.doNextTask();
    }
  }

  /**
   * @return The first <code>PickingRequest</code> in the picking queue or <code>null</code> if the
   *         picking queue is empty.
   */
  PickingRequest getPickingRequest() {
    // System.out.println("Inside getPickingRequest()");
    // System.out.println(pickingQueue);
    return pickingQueue.poll();
  }

  /**
   * @return The first <code>PickingRequest</code> in the sequencing queue or <code>null</code> if
   *         the sequencing queue is empty.
   */
  PickingRequest getSequencingRequest() {
    return sequencingQueue.poll();
  }

  /**
   * @return The first <code>PickingRequest</code> in the loading queue or <code>null</code> if the
   *         loading queue is empty.
   */
  PickingRequest getLoadingRequest() {
    if (readyToLoad()) {
      return loadingQueue.poll();
    }
    return null;
  }

  /**
   * @return A <code>String</code> corresponding to the SKU of the next fascia to be replenished, or
   *         <code>null</code> if the replenishing queue is empty.
   */
  String getReplenishingRequest() {
    return replenishQueue.poll();
  }


  /**
   * @return <code>true</code> if the next <code>PickingRequest</code> in the loading order has been
   *         successfully sequenced.
   */
  boolean readyToLoad() {
    PickingRequest nextInLine = loadingOrder.peek();
    PickingRequest nextSequenced = loadingQueue.peek();

    return (nextInLine != null) && (nextSequenced.equals(nextInLine));
  }

  /**
   * Removes the <code>PickingRequest</code> from the queuing system and logs its completion in the
   * file "orders.csv" in the present working directory. If "orders.csv" exists already, the new
   * content is appended to the end of the file.
   * @param req - The picking request being marked as completed.
   */
  void completeRequest(PickingRequest req) {
    PickingRequest nextInLine = loadingOrder.peek();

    if (!req.equals(nextInLine)) {
      logger.error(String.format(
          "Cannot load PickingRequest #%d; " + "PickingRequest #%d must be loaded first",
          req.getId(), nextInLine.getId()));
    }

    loadingOrder.pop(); // allow the next Order to be loaded
    ArrayList<Order> orders = req.getOrders();
    BufferedWriter bw;
    FileWriter fw;

    try {
      fw = new FileWriter("orders.csv", true);
      bw = new BufferedWriter(fw);
      for (Order o : orders) {
        bw.write(o.toString());
      }
      bw.close();
      fw.close();

    } catch (IOException e) {
      logger.error(String.format(
          "Completed orders from PickingRequest #%d" + "could not be written to orders.csv",
          req.getId()), e);
    }
  }

  void queueWorker(Worker w) {
    wm.enqueueWorker(w);
  }
}
