package project;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class OrderManagerTest {
  RequestManager rm;
  OrderManager om;
  
  @Before public void setUp() throws FileNotFoundException {
    rm = new RequestManager(new WorkerManager());
    om = new OrderManager("translation.csv", rm);
  }
  
  @After public void tearDown() {
    om.clearQueuedOrders();
  }
  
  @Test(expected = FileNotFoundException.class)
  public void testTranslationTableFileNotFound() throws FileNotFoundException {
    om = new OrderManager("afilenamelikethisshouldnt.existinthedirectory", rm);
  }
  
  @Test
  public void testEnqueueOneOrder() {
    int before = om.getQueuedOrders().size();
    Order o1 = new Order("Green", "SES");
    om.enqueue(o1);
    int after = om.getQueuedOrders().size();
    assertEquals(before + 1, after);
  }
  
  @Test
  public void testGenerateLoadingOrder() {
    ArrayList<Order> orders = new ArrayList<>();
    orders.add(new Order("Green", "SES"));
    orders.add(new Order("Red", "S"));
    orders.add(new Order("Graphite", "SEL"));
    orders.add(new Order("Red", "S"));
    ArrayList<String> r = om.generateLoadingOrder(orders);
    assertEquals(r.size(), 8);
  }
  
  @Test
  public void testQueuedOrdersEmptyingAtCapacity() {
    om.enqueue(new Order("Green", "SES"));
    om.enqueue(new Order("Red", "S"));
    om.enqueue(new Order("Graphite", "SEL"));
    om.enqueue(new Order("Red", "S"));
    assertEquals(om.getQueuedOrders().size(), 0);
  }
  
  @Test
  public void testOrderManSendsNewRequestToRequestMan() {
    final int initialSize = rm.getPickingQueue().size();
    om.enqueue(new Order("Green", "SES"));
    om.enqueue(new Order("Red", "S"));
    om.enqueue(new Order("Graphite", "SEL"));
    om.enqueue(new Order("Red", "S"));
    int finalSize = rm.getPickingQueue().size();
    assertEquals(initialSize + 1, finalSize);
  }
    
}
