package project;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

public class WarehousePickingTest {

  @Test
  public void testOptimizeDifferentValues() {
	 ArrayList<String> list = new ArrayList<String>();
	 ArrayList<String> skus = new ArrayList<String>();
	 skus.add("5");
	 skus.add("9");
	 skus.add("1");
    list = WarehousePicking.optimize(skus);
    ArrayList<String> expected = new ArrayList<String>();
    expected.add("1");
    expected.add("5");
    expected.add("9");
    assertTrue(list.equals(expected));
  }
  
  @Test
  public void testOptimizeDuplicateValues() {
	 ArrayList<String> list = new ArrayList<String>();
	 ArrayList<String> skus = new ArrayList<String>();
	 skus.add("5");
	 skus.add("5");
	 skus.add("1");
    list = WarehousePicking.optimize(skus);
    ArrayList<String> expected = new ArrayList<String>();
    expected.add("1");
    expected.add("5");
    expected.add("5");
    assertTrue(list.equals(expected));
  }
  
  @Test
  public void testOptimizeSkuNotInTable() {
	 ArrayList<String> list = new ArrayList<String>();
	 ArrayList<String> skus = new ArrayList<String>();
	 skus.add("ABC");
    list = WarehousePicking.optimize(skus);
    ArrayList<String> emptyList = new ArrayList<String>();
    assertTrue(list.equals(emptyList));
  }

}
