package project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract class of worker in the warehouse.
 */
public abstract class Worker {
  /** The identity of the worker. */
  protected String identity;
  /** The manager that assigns tasks to this worker. */
  protected RequestManager rm;
  /** The manager that updates things as this worker works. */
  protected InventoryManager im;
  /** The availability of the worker. */
  protected boolean isAvail;
  /** The job/type of the worker. */
  protected String type;
  /** The current task that the non-replenisher worker is working on. */
  protected PickingRequest currReq;
  /** The current task that the replenisher worker is working on. */
  protected String replenisherReq;
  /** The index of the current fascia that the worker is working on. */
  protected Integer currIndex;

  private static final Logger logger = LoggerFactory.getLogger(Worker.class);

  /**
   * Instantiates a Worker.
   * 
   * @param identity - the id of the worker.
   * @param rm - the manager that assigns tasks to this worker.
   * @param im - the manager that updates things as this worker works.
   */
  public Worker(String identity, RequestManager rm, InventoryManager im) {
    this.identity = identity;
    this.rm = rm;
    this.im = im;
    isAvail = true;
    currReq = null;
    replenisherReq = "";
    // type = "Worker";
    currIndex = 0;
  }

  /**
   * Worker finishes current Task, resetting isAvail, currReq,
   * replenisherReq, and currIndex to their default values.
   */
  protected void finishTask() {
    logDone();
    isAvail = true;
    currReq = null;
    replenisherReq = "";
    currIndex = 0;
  }

  /**
   * Request a Task if this worker is available.
   */
  protected void doNextTask() {
    logStatus();

    if (isAvail) {
      // System.out.println(currReq);
      requestTask();
      // System.out.println(((PickingRequest)currReq).getId());
    }
  }

  /**
   * Declare the availability of this worker, and work on Task if available.
   */
  protected void requestTask() {
    currReq = (PickingRequest) getRequest();

    if (currReq != null) {
      logAssignedTask("PickingRequest #" + currReq.getId());
      isAvail = false;
      process();
    } else {
      logSys(String.format("%s %s requests a new task", type, identity));
      rm.queueWorker(this);
    }
  }
  
  /**
   * Returns availability of the worker.
   */
  public Boolean getIsAvail() {
    return isAvail;
  }

  public PickingRequest getCurrReq() {
    return currReq;
  }

  public String getReplenisherReq() {
    return replenisherReq;
  }

  public Integer getCurrIndex() {
    return currIndex;
  }

  public String getType() {
    return type;
  }

  /**
   * The process in which this worker takes for a given picking request.
   */
  protected abstract void process();

  /**
   * Gets a request from a worker type specific request queue.
   */
  protected abstract Object getRequest();

  /**
   * The process in which this worker takes for a given picking request.
   */
  protected abstract void sendNextRequest();

  protected void logStatus() {
    if (isAvail) {
      logger.info("{} {} is waiting for tasks to be available", type, identity);
    } else {
      logger.info("{} {} is no longer available; Already working on a task", type, identity);
    }
  }

  protected void logAssignedTask(String task) {
    logger.info("{} {} is assigned task: {}", type, identity, task);
  }

  protected void logNoTask() {
    logger.info("{} {} cannot {} without first being assigned a Request", type,
        identity, type.substring(0, type.length() - 2));
  }

  protected void logError(String error) {
    logger.warn("{} {} reporting an error: {}", type, identity, error);
  }

  protected void logScan(String fasciaSku) {
    logger.info("{} {} scanned fascia with SKU# {}", type, identity, fasciaSku);
  }

  protected void logDone() {
    logger.info("{} {} Completed Task", type, identity);
  }

  protected void logRequesting(String request) {
    logger.info("{} {} Requesting --{}-- event for PickingRequest #{}", type, identity,
        request, currReq.getId());
  }

  protected void logSys(String m) {
    logger.info("System: {}", m);
  }

  /**
   * Returns a string representation of the worker, depending on availability.
   */
  public String toString() { // for debug purposes
    String strRep = String.format("%s %s", type, identity);

    if (!isAvail) {
      strRep += String.format(" working on PickingRequest #%d", currReq.getId());
    }
    return strRep;
  }
}
