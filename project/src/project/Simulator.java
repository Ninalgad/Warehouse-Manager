package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Simulator {
  private RequestManager rm;
  private OrderManager om;
  private InventoryManager im;
  private HashMap<String, Worker> directory;

  private List<Pattern> commandPatterns = Arrays.asList(
      // Orders
      Pattern.compile("^Order \\w+ \\w+$"),

      // All workers
      Pattern.compile("^(Picker|Sequencer|Loader|Replenisher) \\w+ ready$"),

      // Pickers
      Pattern.compile("^Picker \\w+ picks \\w+$"), Pattern.compile("^Picker \\w+ to Marshaling$"),

      // Sequencers
      Pattern.compile("^Sequencer \\w+ sequences$"), Pattern.compile("^Sequencer \\w+ rescans$"),
      Pattern.compile("^Sequencer \\w+ rejects$"), Pattern.compile("^Sequencer \\w+ approves$"),

      // Loaders
      Pattern.compile("^Loader \\w+ loads$"), Pattern.compile("^Loader \\w+ rescans$"),
      Pattern.compile("^Loader \\w+ rejects$"), Pattern.compile("^Loader \\w+ approves$"),

      // Replenisher
      Pattern.compile("^Replenisher \\w+ replenishes$"));

  private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

  /**
   * Create a new warehouse simulation.
   * 
   * @throws FileNotFoundException if translation.csv or initial.csv are not in the working
   *         directory.
   */
  public Simulator() throws FileNotFoundException {
    rm = new RequestManager(new WorkerManager());
    om = new OrderManager("translation.csv", rm);
    im = new InventoryManager("initial.csv", rm, true);
    directory = new HashMap<String, Worker>();
  }

  Simulator(RequestManager rm, OrderManager om, InventoryManager im) {
    this.rm = rm;
    this.om = om;
    this.im = im;
    directory = new HashMap<String, Worker>();
  }

  /**
   * Simulates a series of warehouse events, printing output to log.txt and to STDOUT.
   * 
   * @param commandList - The warehouse events to simulate.
   */
  public void runSimulation(ArrayList<String> commandList) throws IOException {
    for (String command : commandList) {

      logger.debug(String.format("Command: %s", command));
      parseCommand(command);
    }
    im.saveInventory();
  }

  /**
   * Checks if a command is valid, and passes any valid command to the appropriate parser method.
   */
  void parseCommand(String command) {
    if (isValidCommand(command)) {
      String[] words = command.split(" ");
      if (Pattern.matches("^Order \\w+ \\w+$", command)) {
        processOrder(words[2], words[1]);
      } else {
        parseWorkerCommand(command);
      }
    } else {
      logInvalidCommand(command);
    }
  }

  boolean isValidCommand(String command) {
    for (Pattern p : commandPatterns) {
      if (p.matcher(command).matches()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates and enqueues a new Order in the simulation. Logs an error if the Order is invalid.
   * 
   * @param colour - The colour specified in simulation command.
   * @param model - The model specified in the simulation command.
   */
  private void processOrder(String colour, String model) {
    if (om.validOrder(colour, model)) {
      Order order = new Order(colour, model);
      om.enqueue(order);
    } else {
      logger.warn("Invalid Order command: 'Colour {}, Model {}' is not a recognized combination",
          colour, model);
    }
  }

  /**
   * Interprets and simulates an event involving a Worker.
   */
  private void parseWorkerCommand(String command) {
    String type = command.split(" ")[0];
    String id = command.split(" ")[1];

    if (Pattern.matches("\\w+ \\w+ ready", command)) {
      if (workerExists(id, type) == null) {
        logger.warn(id + " already exists, but is not working as a " + type);
      } else if (!workerExists(id, type)) {
        createWorker(id, type);
      }
      directory.get(id).doNextTask();

    } else if (workerExists(id, type)) { //&& (!directory.get(id).getIsAvail())) {
      switch (type) {
        case "Picker":
          parsePickerCommand(command);
          break;
        case "Sequencer":
          parseSequencerCommand(command);
          break;
        case "Loader":
          parseLoaderCommand(command);
          break;
        case "Replenisher":
          parseReplenisherCommand(command);
          break;
        default:
          logInvalidCommand(command);
      }
    }
  }


  /**
   * Informs the system that the specified worker is ready.
   * 
   * @precondition worker exists
   */
  void createWorker(String id, String type) {
    switch (type) {
      case ("Picker"):
        directory.put(id, new Picker(id, rm, im));
        break;
      case ("Loader"):
        directory.put(id, new Loader(id, rm, im));
        break;
      case ("Sequencer"):
        directory.put(id, new Sequencer(id, rm, im));
        break;
      case ("Replenisher"):
        directory.put(id, new Replenisher(id, rm, im));
        break;
      default:
        logger.error("Cannot create worker of type: " + type);
    }
  }

  private void parsePickerCommand(String line) {
    String[] lineSplit = line.split(" ");
    String id = lineSplit[1];

    if (Pattern.matches("^Picker " + id + " picks \\w+$", line)) {
      String sku = line.split(" ")[3];
      ((Picker) directory.get(id)).pick(sku);
    } else if (Pattern.matches("^Picker " + id + " to Marshaling$", line)) {
      directory.get(id).sendNextRequest();
    } else {
      logInvalidCommand(line);
    }
  }

  private void parseSequencerCommand(String command) {
    String id = command.split(" ")[1];
    String action = command.split(" ")[2];
    switch (action) {
      case "sequences":
        ((CheckerWorker) directory.get(id)).check();
        break;
      case "rescans":
        ((CheckerWorker) directory.get(id)).rescan();
        break;
      case "rejects":
        ((CheckerWorker) directory.get(id)).sendToRepick();
        break;
      case "approves":
        ((CheckerWorker) directory.get(id)).sendNextRequest();
        break;
      default:
        logInvalidCommand(command);
    }
  }

  private void parseLoaderCommand(String command) {
    String id = command.split(" ")[1];
    String action = command.split(" ")[2];
    switch (action) {
      case "loads":
        ((CheckerWorker) directory.get(id)).check();
        break;
      case "rescans":
        ((CheckerWorker) directory.get(id)).rescan();
        break;
      case "rejects":
        ((CheckerWorker) directory.get(id)).sendToRepick();
        break;
      case "approves":
        ((CheckerWorker) directory.get(id)).sendNextRequest();
        break;
      default:
        logInvalidCommand(command);
    }
  }


  private void parseReplenisherCommand(String command) {
    String id = command.split(" ")[1];
    ((Replenisher) directory.get(id)).replenish();
  }

  private void logInvalidCommand(String line) {
    logger.warn("Command '{}' could not be recognized. Please reformat this command.", line);

  }

  /**
   * @return <code>true</code> if a worker with id <code>id</code> and position <code>type</code>
   *         already exists in <code>map</code>.
   */
  Boolean workerExists(String id, String type) {
    // System.out.println(id + type);
    if (!directory.containsKey(id)) { // check if id exists in directory
      return false;

    } else if (directory.get(id).getType().equals(type)) { // check if id matches type
      return true;

    } else { // special case where worker's id exists, but still not the worker in question
      return null;
    }
  }

  public HashMap<String, Worker> getDirectory() {
    return directory;
  }

  /**
   * Manually adds a worker to the worker directory. Use only for testing.
   * 
   * @param id - The unique ID of the worker.
   * @param worker - The Worker object corresponding to id.
   */
  void addWorker(String id, Worker worker) {
    directory.put(id, worker);
  }

  /**
   * Manually add order to order manager. Use only for testing.
   */
  void addOrder(Order order) {
    om.enqueue(order);
  }

}
