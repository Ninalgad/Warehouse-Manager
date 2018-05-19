package project;


public class Replenisher extends Worker {

  /**
   * Instantiates a Replenisher Worker.
   */
  public Replenisher(String identity, RequestManager rm, InventoryManager im) {
    super(identity, rm, im);
    type = "Replenisher";
  }

  @Override
  protected void process() {
    // currently this does nothing
  }

  @Override
  protected void requestTask() {
    // this method got override b/c this worker use request of type String instead
    String currentTask = (String) getRequest();

    if (currentTask != null) {
      replenisherReq = currentTask;
      isAvail = false;
      logAssignedTask("Replenishing fascias at location: " + currentTask);
      process();
    } else {
      rm.queueWorker(this);
    }
  }

  /**
   * Modify the format of the location of the fascia to be replenished. Tell the im to update its
   * records.
   */
  protected void replenish() {
    if (replenisherReq.equals("")) {
      System.out.println("before log");
      logNoTask();
      System.out.println("no task");
    } else {
      if (!im.replenish(replenisherReq)) {
        logSys("Location " + replenisherReq + " has enough fascias (5+); no need to replenish");
      }
      sendNextRequest(); // this is really just a call to finishTask();
    }

  }

  @Override
  protected String getRequest() {
    return rm.getReplenishingRequest();
  }

  @Override
  protected void sendNextRequest() {
    finishTask();
  }

  @Override
  public String toString() {
    String strRep = String.format("%s %s", type, identity);

    if (!isAvail) {
      strRep += String.format(" replenishing location: %s", replenisherReq);
      return strRep;
    }

    return strRep;
  }
}
