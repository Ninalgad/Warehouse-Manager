package project;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InventoryManagerTest {
  
  protected RequestManager rm = new RequestManager(new WorkerManager());
  protected InventoryManager im;
  
  @Before public void setUp() throws FileNotFoundException {
    im = new InventoryManager("initial.csv", rm, false);
  }
  
  @After public void afterAlltearDown() {
    rm = null;
    im = null;
  }
  

  @Test(expected = FileNotFoundException.class)
  public void testInventoryManagerStringRequestManagerBoolean() throws FileNotFoundException {
    im = new InventoryManager("intentionally_incorrect_path_to nothing", rm, false);
  }
  
  @Test
  public void testDecrement() { 
    Iterator<String> iter = im.getLocations().keySet().iterator();
    if (iter.hasNext()) {
      String sku = iter.next();
      System.out.println(sku);
      int expected = im.getInventory().get(sku) - 1;
      im.decrement(sku);
      int actual = im.getInventory().get(sku);
      assertEquals(actual, expected);
    }
  }

  @Test
  public void testReplenish() throws FileNotFoundException {
    im.getLocations().put("SampleSKU_1", "Sample_Location_1");
    im.getSkus().put("Sample_Location_1", "SampleSKU_1");
    im.getInventory().put("SampleSKU_1", 3);
    
    im.getLocations().put("SampleSKU_2", "Sample_Location_2");
    im.getSkus().put("Sample_Location_2", "SampleSKU_2");
    im.getInventory().put("SampleSKU_2", 21);
    
    int expected = 28;
    im.replenish("Sample_Location_1");
    int actual = im.getInventory().get("SampleSKU_1");
    assertEquals(actual, expected);
    
    expected = 21;
    im.replenish("Sample_Location_2");
    actual = im.getInventory().get("SampleSKU_2");
    assertEquals(actual, expected);
    
  }

  @Test
  public void testGetAmount() throws FileNotFoundException {
    im.getLocations().put("SampleSKU_1", "Sample_Location_1");
    im.getSkus().put("Sample_Location_1", "SampleSKU_1");
    im.getInventory().put("SampleSKU_1", 3);
    
    int expected = 3;
    int actual = im.getAmount("SampleSKU_1");
    assertEquals(expected, actual);
  }

  @Test
  public void testGetInventory() throws FileNotFoundException {
    int expected = im.getInventory().size() + 1;
    im.getInventory().put("Sample_Sku", 7);
    int actual = im.getInventory().size();
    assertEquals(expected, actual);
  }

  @Test
  public void testGetLocations() throws FileNotFoundException {
    int expected = im.getLocations().size() + 1;
    im.getLocations().put("Sample_Sku", "Sample_Location");
    int actual = im.getLocations().size();
    assertEquals(expected, actual);
  }

  @Test
  public void testGetSkus() throws FileNotFoundException {
    int expected = im.getSkus().size() + 1;
    im.getSkus().put("Sample_Location", "Sample_Sku");
    int actual = im.getSkus().size();
    assertEquals(expected, actual);
  }
}