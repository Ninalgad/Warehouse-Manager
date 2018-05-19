package project;

public class Sequencer extends CheckerWorker {
  /**
   * Instantiates a Sequencer Worker.
   */
  public Sequencer(String identity, RequestManager rm, InventoryManager im) {
    super(identity, rm, im);
    type = "Sequencer";
  }

  @Override
  protected PickingRequest getRequest() {
    return rm.getSequencingRequest();
  }

  @Override
  protected void sendNextRequest() {
    if (currReq == null) {
      logError("I don't have any PickingRequests to Approve");

    } else {
      // put things in the correct arrangement
      currReq.clearFasciaState();

      for (String sku : currReq.getLoadingOrder()) {
        currReq.updateFasciaState(sku);
      }

      logRequesting("Loading");
      rm.sendToLoading(currReq);

      clearValidFascias();
      finishTask();
    }
  }

}
