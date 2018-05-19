import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import project.Simulator;

public class Main {
  
  /**
   * Simulates the warehouse events listed in the file passed as an argument.
   * @param args - The event file to simulate.
   * @throws FileNotFoundException if the provided file does not exist.
   */
  public static void main(String[] args) throws IOException {
    File completedOrderLog = new File("orders.csv");
    if (completedOrderLog.isFile()) {
      completedOrderLog.delete();
    }
    
    Simulator simulator = new Simulator();
    simulator.runSimulation(getCommands(args[0]));
  }
  
  /**
   * @param commandFile - The name of the command file in the working directory.
   * @throws FileNotFoundException if commandFile is not in the working directory.
   */
  private static ArrayList<String> getCommands(String commandFile) throws FileNotFoundException {
    ArrayList<String> commandList = new ArrayList<>();
    Scanner scanner = new Scanner(new FileInputStream("zmd_cmd_err.txt"));//commandFile));
    while (scanner.hasNextLine()) {
      String command = scanner.nextLine();
      commandList.add(command);
    }
    scanner.close();
    return commandList;
  }
}
