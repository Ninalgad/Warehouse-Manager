package project;

import java.util.ArrayList;

public abstract class CheckerWorker extends Worker {

  /** List of expected SKUs in the current request. */
  ArrayList<String> expectedFascias;

  /** List of SKUs actually present in the current request. */
  ArrayList<String> givenFascias;

  /**
   * A list containing the boolean results of comparing expected fascia with the actual fascia
   * present in the current request..
   */
  ArrayList<Boolean> validFascias;

  /** The current task this Worker is working on. */
  PickingRequest request;

  /**
   * Instantiates a Checker Worker.
   */
  public CheckerWorker(String identity, RequestManager rm, InventoryManager im) {
    super(identity, rm, im);
    validFascias = new ArrayList<Boolean>();
    currIndex = 0;
    clearValidFascias();
  }

  @Override
  protected void process() {
    expectedFascias = currReq.getLoadingOrder();
    givenFascias = currReq.getFasciaState();
  }

  /**
   * Grants the worker another attempt to verify all SKUs in the current request.
   */
  protected void rescan() {
    logSys(type + identity + " rescanning");
    currIndex = 0;
    clearValidFascias();
  }

  /**
   * Checks if the SKUs physically present match the expect SKUs for the current request.
   */
  protected void check() {
    if (currReq == null) {
      logNoTask();
    } else if (currIndex < 8) {
      if (validFascias.contains(false)) {
        logSys("There's already a misplaced fascia, RESCAN or REJECT");
      } else {
        // this makes sure repeating sku numbers are checked properly
        Boolean isValid = givenFascias.contains(expectedFascias.get(currIndex));

        logScan(expectedFascias.get(currIndex));

        if (isValid) {
          logSys("Scanned fascia is expected; Correct");
        } else {
          logSys("Scanned fascia is not expected;\nThe expected fascia with SKU#: "
              + expectedFascias.get(currIndex) + " is not given. RESCAN or REJECT");
        }

        validFascias.add(isValid);

        currIndex++;
      }
    } else {
      logError("I already checked all 8 fasicas");
    }
  }

  protected void clearValidFascias() {
    for (int i = 0; i < validFascias.size(); i++) {
      validFascias.set(i, null);
    }
  }

  /**
   * Removes current request from this worker and sends it back to the picking queue.
   */
  protected void sendToRepick() {
    logRequesting("Repick");
    rm.sendToPicking(currReq);

    clearValidFascias();
    finishTask();
  }

  protected ArrayList<String> getExpectedFascias() {
    return expectedFascias;
  }

  protected ArrayList<String> getGivenFascias() {
    return givenFascias;
  }

  protected ArrayList<Boolean> getValidFascias() {
    return validFascias;
  }
}
