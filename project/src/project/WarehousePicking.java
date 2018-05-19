package project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class WarehousePicking {
  /**
   * Based on the Integer SKUs in List 'skus', return the same list of skus but in optimized order.
   * 
   * @param skus the list of SKUs to retrieve.
   * @return the List of locations.
   */
  public static ArrayList<String> optimize(ArrayList<String> skus) {
    Scanner scanner = null;
    String[] lineList;
    ArrayList<String> returnList = new ArrayList<String>();

    String traversalTable = "traversal_table.csv";
    
    // sku is the key, amount of times a sku appears in the set of fascias is the value
    HashMap<String, Integer> skuAmount = new HashMap<>(80);
    //Changing initial capacity to 80, which is roughly twice the expected number
    //of data entries.
    
    for (String sku : skus) {
      if (skuAmount.containsKey(sku)) {
        skuAmount.put(sku,skuAmount.get(sku) + 1); // +1 to existing
      } else {
        skuAmount.put(sku, 1);
      }
    } // this set up makes sure the following while loop can account for repeated skus codes

    try {
      scanner = new Scanner(new FileInputStream(traversalTable));

      //this prioritizes the fasia that appears first in the traversal_table.csv
      while (scanner.hasNextLine()) { 
        lineList = scanner.nextLine().split(",");
        
        String sku = lineList[4];
 
        if (skus.contains(sku)) { // arrange skus[] in same order as in traversalTable.csv
          for (int i = 0 ; i < skuAmount.get(sku); i++) {
            returnList.add(sku);
          } // so then if a type of fascia is ordered more than once, it will be taken note of
        }
      }
      scanner.close();
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    return returnList;
  }
}
