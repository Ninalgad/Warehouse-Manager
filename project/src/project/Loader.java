package project;

public class Loader extends CheckerWorker {

  /**
   * Instantiates a Loader Worker.
   */
  public Loader(String identity, RequestManager rm, InventoryManager im) {
    super(identity, rm, im);
    type = "Loader";
  }

  @Override
  protected PickingRequest getRequest() {
    return rm.getLoadingRequest();
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
      
      // System.out.println(currReq.getId());
      logRequesting("Completion");
      // rm.readyToLoad();

      // if the request isn't what's suppose to be loaded, then rm will put it back into loadingQ
      rm.completeRequest(currReq);
      clearValidFascias();
      finishTask();
    }
  }


}
