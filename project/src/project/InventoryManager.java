package project;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * Monitors and maintains the number of remaining fascia on the picking floor.
 *
 */
public class InventoryManager {
  // inventory stores SKUs as keys and the remaining fascia of that SKU as the value. 
  private HashMap<String, Integer> inventory = new HashMap<String, Integer>();
  // skus stores locations of fascia as keys and the corresponding SKU as the value. 
  private HashMap<String, String> skus = new HashMap<String, String>();
  // locations stores SKUs of fascia as keys and the corresponding location as the value. 
  private HashMap<String, String> locations = new HashMap<String, String>();

  private RequestManager rm;

  
  /**
   * Instantiates a IventoryManger.
   * 
   * @param filePath               a file name of the .csv of the current inventory.
   * @param rm                     an instance of a <code>RequestManager</code>
   *                               to handle replenishing 
   * @param doCheck                enables initial inventory checks. If true check if we
   *                               start off lacking fascia    
   * @throws FileNotFoundException if <code>initial.csv</code> is not in the working
   *                               directory.
   */
  public InventoryManager(String filePath, RequestManager rm,
      Boolean doCheck) throws FileNotFoundException {
    this.rm = rm;
    initialiseInventory(filePath);
    if (doCheck) {
      this.checkAllFasciaLevels();
    }
  }

  /**
   * Populate the <code>inventoryMap</code> from the file at path <code>filePath</code>.
   * 
   * @param filePath               the path of the data file which provides the current 
   *                               state of the inventory
   * @throws FileNotFoundException if filePath is not a valid path or if 
   *                               <code>traversal_table.csv</code> is not in the working 
   *                               directory 
   */
  private void initialiseInventory(String filePath) throws FileNotFoundException {
    genLocMap("traversal_table.csv");

    Scanner scanner;
    scanner = new Scanner(new FileInputStream(filePath));

    for (String sku : skus.values()) {
      inventory.put(sku, 30); // unspecified/default locations contain full inventory
    }

    String line;
    Integer amount;
    String currLoc;
    String sku;
    while (scanner.hasNextLine()) {
      line = scanner.nextLine();
      amount = Integer.parseInt(line.substring(8));
      currLoc = line.substring(0, 7);
      sku = skus.get(currLoc);
      inventory.put(sku, amount);
    }
    scanner.close();
  }


  /**
   * Generate two complementary <code>hashMap</code>. One has the keys as SKU storing
   * the locations, called <code>locations</code>. The other has the keys as location storing
   * SKUs, called <code>skus</code>.
   * 
   * @param trasversalTable         the path of the data file which provides trasversal table 
   * @throws FileNotFoundException  if <code>trasversalTable</code> is not a valid path
   */
  private void genLocMap(String trasversalTable) throws FileNotFoundException {
    Scanner scanner;
    String line;
    String sku;
    String fasciaLocation;

    scanner = new Scanner(new FileInputStream(trasversalTable));

    while (scanner.hasNextLine()) {

      line = scanner.nextLine();
      sku = line.substring(8, line.length());
      fasciaLocation = line.substring(0, 7);
      skus.put(fasciaLocation, sku);
      locations.put(sku, fasciaLocation);
    }
    scanner.close();
  }


  /**
   * Save the current state of the <code>inventory</code> to <code>final.csv</code>.
   * 
   */
  public void saveInventory() throws IOException {
    String eol = System.getProperty("line.separator");
    Writer writer = new FileWriter("final.csv");
    for (Entry<String, String> entry : skus.entrySet()) {
      if (inventory.get(entry.getValue()) != 30) {
        String location = entry.getKey();
        String amount = inventory.get(entry.getValue()).toString();
        writer.append(location + "," + amount + eol);
      }
    }
    writer.close();
  }


  /**
   * Remove a fascia from the <code>inventory</code> based that the fascia's SKU.
   * 
   * @param sku the unique number specifying a type of fascia.
   */
  public void decrement(String sku) {

    inventory.put(sku, inventory.get(sku) - 1);

    this.checkFasciaLevel(sku);
  }

  /**
   * Update the <code>inventory</code> once a fascia has been replenished.
   * 
   * @param loc the unique location of a specifying of a fascia.
   */
  public Boolean replenish(String loc) {
    String sku = skus.get(loc);

    if (inventory.get(sku) <= 5) {
      inventory.put(sku, inventory.get(sku) + 25);
      return true;
    }
    return false;
  }

  /**
   * Return the number of fascias present at the given SKU.
   * 
   * @param sku the unique location of a specifying of a fascia.
   */
  protected Integer getAmount(String sku) {
    return inventory.get(sku);
  }


  /**
   * Update <code>replenishQueue</code> to request to replenish a fascia.
   * 
   * @param sku the unique number specifying a type of fascia.
   */
  private void sendReplenishRequest(String sku) {
    rm.sendToReplenish(sku);
  }


  /**
   * Ensure the entire <code>inventory</code> is not lacking 
   * any fascia, otherwise request to replenish the lacking fascia.
   * 
   */
  private void checkAllFasciaLevels() {
    for (String key : inventory.keySet()) {
      checkFasciaLevel(key);
    }
  }


  /**
   * Ensure the <code>inventory</code> is not lacking this fascia given its SKU, otherwise request
   * to replenish.
   * 
   * @param sku the unique number specifying a type of fascia.
   */
  private void checkFasciaLevel(String sku) {
    if (inventory.get(sku) <= 5) {
      this.sendReplenishRequest(locations.get(sku));
    }
  }
  
  protected HashMap<String, Integer> getInventory() {
    return inventory;
  }
 
  protected HashMap<String, String> getLocations() {
    return locations;
  }
  
  public HashMap<String, String> getSkus() {
    return skus;
  }

  public String getLocation(String sku) {
    return locations.get(sku);
  }
}
