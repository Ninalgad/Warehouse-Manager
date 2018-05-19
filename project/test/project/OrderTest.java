package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class OrderTest {
  @Before public void initialize() {
    Order.setNextId(1);
  }

  
  @Test
  public void testOrderIdIncreasing() {
    Order o1 = new Order("Red", "SES");
    Order o2 = new Order("Red", "SES");
    assertTrue(o2.getId() > o1.getId());
  }
  
  @Test
  public void testOrderIdStartsAt1() {
    Order o1 = new Order("Red", "SES");
    assertEquals(o1.getId(), 1);
  }

  @Test
  public void testToString() {
    Order o1 = new Order("Red", "SES");
    String expected = "Order #1: Red, SES";
    assertEquals(expected, o1.toString()); 
  }
}
