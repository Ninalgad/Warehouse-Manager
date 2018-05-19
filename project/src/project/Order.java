package project;

/**
 * An order for a pair of front and rear fascia.
 */
public class Order {

  private static int nextId = 1;
  private int id = 0;
  String model;
  String colour;

  /**
   * @param colour - The colour of the fascia specified in the order.
   * @param model - The minivan model of the fascia specified in the order.
   */
  public Order(String colour, String model) {
    this.id = nextId;
    nextId++;
    this.model = model;
    this.colour = colour;
  }
  
  protected static void setNextId(int val) {
    nextId = val;
  }
  
  /**
   * @return String with format "Model,Colour" followed by a line separator.
   */
  public String toString() {
    return String.format("Order #%d: %s, %s", getId(), colour, model);
  }

  public int getId() {
    return id;
  }

}
