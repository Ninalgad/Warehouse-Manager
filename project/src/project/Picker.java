package project;

import java.util.ArrayList;

public class Picker extends Worker {
  /** The 8 fascias that are in the arrangement that gives optimized picking. */
  private ArrayList<String> optPath;

  private Boolean donePicking;

  /**
   * Instantiates a Picker Workers.
   */
  public Picker(String identity, RequestManager rm, InventoryManager im) {
    super(identity, rm, im);
    type = "Picker";
    donePicking = false;;
  }

  @Override
  protected void process() {
    optPath = currReq.getPickingOrder();
  }

  /**
   * tell the im to update its record of the fascias. log the scanned fascia sku. log invalid sku if
   * picker is told to get the wrong fascia
   * 
   * @param sku - the sku of the fascia that the picker is picking right now
   */
  protected void pick(String sku) {
    if (currReq == null) {
      logNoTask();

    } else if (!donePicking) {
      // check if command sku is same as the one that system gives

      logScan(sku);
      if (optPath.contains(sku) && optPath.get(currIndex).equals(sku)) { // java uses smart compare
        currReq.updateFasciaState(sku);

        im.decrement(sku);
        logSys(String.format("Fascia with SKU #%s is picked at %s", sku, im.getLocation(sku)));

        currIndex++;

        checkDonePicking();
      } else {
        logError("This fascia is different from what the system told me to pick");
        logSys("Please get fascia with sku #" + optPath.get(0));
      }

    } else { // if done picking
      logError("Already done picking 8 fascias");
    }

  }

  @Override
  protected void sendNextRequest() {
    if (currReq == null) {
      logError("I don't need to go to Marshaling without a PickingRequest");

    } else if (donePicking) { // Picker going to sequencing and dropping off picked fascia
      logRequesting("Sequencing");
      rm.sendToSequencing(currReq);
      finishTask();
      optPath = null;
      donePicking = false;
    } else {
      logError("I haven't finished picking 8 fasicas yet, so I shouldn't go to Marshaling area");
    }
  }

  @Override
  protected PickingRequest getRequest() {
    return rm.getPickingRequest();
  }

  private Boolean checkDonePicking() {
    if (currIndex.equals(8)) {
      donePicking = true;
      return true;
    }
    return false;
  }

  protected ArrayList<String> getPickingOrder() {
    return optPath;
  }
}
