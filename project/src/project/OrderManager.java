package project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects incoming orders for fascia and groups them into picking requests.
 *
 */
public class OrderManager {

  /**
   * The queue of orders to be packaged into the next <code>PickingRequest</code>.
   *
   *
   * <p>The queue <code>orders</code> has a capacity of 3 <code>Order</code>s. When a fourth order
   *  is offered to <code>orders</code>, the queue is emptied and packaged as a
   * <code>PickingRequest</code> with the fourth <code>Order</code>
   */
  private ArrayBlockingQueue<Order> queuedOrders = new ArrayBlockingQueue<>(3);

  /**
   * The <code>RequestManager</code> to which this <code>OrderManager</code> sends its
   * <code>PickingRequest</code>s.
   */
  private RequestManager requestManager;

  /**
   * A nested <code>HashMap</code> associating minivan colours and models to the corresponding SKUs
   * of the front and rear fascia.
   */
  private HashMap<String, HashMap<String, String[]>> fasciaMap;

  private static final Logger logger = LoggerFactory.getLogger(OrderManager.class);

  /**
   * @param translationTable - <code>String</code> file path to the flat file associating minivan
   *        colours and models to fascia SKUs.
   * @param man - This <code>OrderManager</code> will send <code>PickingRequests</code> to this
   *        <code>RequestManager</code>
   * @throws FileNotFoundException if <code>translationTable</code> is not present in the working
   *         directory.
   */
  public OrderManager(String translationTable, RequestManager man) throws FileNotFoundException {
    this.requestManager = man;
    this.fasciaMap = new HashMap<String, HashMap<String, String[]>>(80);
    populateFasciaMap(translationTable);
  }

  /**
   * Reads the provided translation table to assign key-value pairs to
   * <code>OrderManager.fasciaMap</code>.
   *
   *
   * <p>Precondition: the provided CSV file contains one header line, followed by one string of
   * the format <code>Colour,Model,FrontSKU,RearSKU</code> on each subsequent line.
   *
   * @param translationTable - The CSV file containing the fascia data in the specified format
   */
  private void populateFasciaMap(String translationTable) throws FileNotFoundException {
    try {
      HashMap<String, HashMap<String, String[]>> outerMap = fasciaMap;
      Scanner scanner = new Scanner(new FileInputStream(translationTable));
      scanner.nextLine(); // skip the first line in the file

      while (scanner.hasNextLine()) {
        String rawLine = scanner.nextLine();
        String[] splitLine = rawLine.split(",");
        
        String colour = splitLine[0];
        String model = splitLine[1];
        String frontSku = splitLine[2];
        String rearSku = splitLine[3];
        String[] skuArray = {frontSku, rearSku};

        if ((outerMap).containsKey(colour)) {
          HashMap<String, String[]> existingInnerMap = outerMap.get(colour);
          existingInnerMap.put(model, skuArray);
        } else {
          HashMap<String, String[]> newInnerMap = new HashMap<>(8);
          newInnerMap.put(model, skuArray);
          fasciaMap.put(colour, newInnerMap);
        }
      }
      scanner.close();
    } catch (FileNotFoundException e) { 
      logger.error(String.format("The file '%s' was not found in the working directory.",
                               translationTable), e);
      throw e;
    }
  }

  /**
   * Adds an <code>Order</code> to the order queue. If the queue is full, empties the elements from
   * the queue into a new <code>PickingRequest</code> along with <code>order</code>.
   *
   * @param order - The order to be added to the orderQueue.
   */
  public void enqueue(Order order) {
    logger.info(String.format("New order queued for processing: %s", order.toString()));
    boolean orderAdded = queuedOrders.offer(order);

    if (!orderAdded) {
      // create array of queuedOrders and order
      ArrayList<Order> orderGroup = new ArrayList<>(queuedOrders);
      orderGroup.add(order);

      // create PickingRequest from the array
      ArrayList<String> loadingOrder = generateLoadingOrder(orderGroup);
      PickingRequest newRequest = new PickingRequest(orderGroup, loadingOrder);
      logger.info(String.format("PickingRequest %d created (Orders %s)",
                                            newRequest.getId(), newRequest.getOrderString()));

      // add the new PickingRequest to the queue system and clear the queuedOrders
      requestManager.addRequest(newRequest);
      queuedOrders.clear();
    }
  }

  /**
   * @param orderGroup - The four <code>Order</code>s to be loaded in the order they were received.
   * @return The SKUs of all fascia to be loaded, in the order they must be loaded.
   */
  ArrayList<String> generateLoadingOrder(ArrayList<Order> orderGroup) {
    ArrayList<String> frontFascia = new ArrayList<>();
    ArrayList<String> rearFascia = new ArrayList<>();
    for (Order order : orderGroup) {
      String colour = order.colour;
      String model = order.model;
      String[] skuArray = fasciaMap.get(colour).get(model);
      frontFascia.add(skuArray[0]);
      rearFascia.add(skuArray[1]);
    }

    // Proper loading order: (truck front) --> [R1, R2, R3, R4] [F1, F2, F3, F4] <-- (truck rear)
    rearFascia.addAll(frontFascia);
    return rearFascia;
  }

  /**
   * @return true if the warehouse stocks fascia corresponding to the given colour and model.
   */
  boolean validOrder(String color, String model) {
    if (fasciaMap.containsKey(color)) {
      if (fasciaMap.get(color).containsKey(model)) {
        return true;
      }
    }
    return false;
  }

  public ArrayBlockingQueue<Order> getQueuedOrders() {
    return queuedOrders;
  }

  protected void clearQueuedOrders() {
    queuedOrders.clear();
  }

}
