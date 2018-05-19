package project;

import java.util.ArrayList;

public class PickingRequest implements Comparable<PickingRequest> {

  private static int nextId = 1;
  private int id;

  /** The 8 orders grouped by this request. */
  private ArrayList<Order> orderGroup;

  /** The SKU numbers of the 8 fascia contained in this request in proper loading order. */
  private ArrayList<String> loadingOrder;

  /** The locations of the 8 fascia in this PickingRequest in the most efficient picking order. */
  private ArrayList<String> pickingOrder;

  /** The state of the fascia in the current order. */
  private ArrayList<String> fasciaState;

  /**
   * Initialize a PickingRequest using two ArrayLists orderGroup and loadingOrder.
   * 
   * @param orderGroup ArrayList containing a group of orders.
   * @param loadingOrder ArrayList containing a group of orders.
   */
  public PickingRequest(ArrayList<Order> orderGroup, ArrayList<String> loadingOrder) {
    this.orderGroup = orderGroup;
    this.loadingOrder = loadingOrder;
    this.pickingOrder = WarehousePicking.optimize(loadingOrder);
    this.id = nextId;
    nextId++;
    this.fasciaState = new ArrayList<String>();
    // status();
  }
  
  public PickingRequest(int id) {
    this.id = id;
    this.fasciaState = new ArrayList<String>();
  }

  void updateFasciaState(String sku) {
    this.fasciaState.add(sku);
  }

  ArrayList<String> getFasciaState() {
    return this.fasciaState;
  }

  void clearFasciaState() {
    this.fasciaState.clear();
  }

  int getId() {
    return this.id;
  }

  ArrayList<String> getLoadingOrder() {
    return this.loadingOrder;
  }

  ArrayList<String> getPickingOrder() {
    return this.pickingOrder;
  }

  ArrayList<Order> getOrders() {
    return this.orderGroup;
  }


  public int compareTo(PickingRequest other) {
    return Integer.compare(this.id, other.id);
  }

  /**
   * @return The Order ID numbers of the Orders in this request as a comma-separated String.
   */
  public String getOrderString() {
    String s = "";
    for (Order o : orderGroup) {
      s += String.valueOf(o.getId()) + ", ";
    }
    return s.substring(0, s.length() - 2);
  }
}
