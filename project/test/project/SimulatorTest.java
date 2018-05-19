package project;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)

public class SimulatorTest {
  private Simulator sim;
  private HashMap<String, Worker> dir;
  
  @Mock RequestManager rmMock;
  @Mock OrderManager omMock;
  @Mock InventoryManager imMock;
  
  @Mock Picker mockPicker;
  @Mock Sequencer mockSequencer;
  @Mock Loader mockLoader;
  @Mock Replenisher mockReplenisher;

  /**
   * @throws FileNotFoundException if initial.csv or translation.csv are not in the working
   *         directory.
   */
  @Before
  public void initialize() {
    try {
      sim = new Simulator();
      dir = new HashMap<>();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Rule
  public LogbackVerifier logbackVerifier = new LogbackVerifier();

  @Test
  public void testCreateWorkerAcceptsPickers() {  
    assertFalse(dir.containsKey("Francis"));
    sim.createWorker("Francis", "Picker");    
    dir = sim.getDirectory();
    assertTrue(dir.containsKey("Francis"));
    assertTrue(dir.get("Francis") instanceof Picker);
  }

  @Test
  public void testCreateWorkerAcceptsSequencers() {  
    assertFalse(dir.containsKey("Freda"));
    sim.createWorker("Freda", "Sequencer");    
    dir = sim.getDirectory();
    assertTrue(dir.containsKey("Freda"));
    assertTrue(dir.get("Freda") instanceof Sequencer);
  }

  @Test
  public void testCreateWorkerAcceptsLoaders() {  
    assertFalse(dir.containsKey("Fred"));
    sim.createWorker("Fred", "Loader");    
    dir = sim.getDirectory();
    assertTrue(dir.containsKey("Fred"));
    assertTrue(dir.get("Fred") instanceof Loader);
  }

  @Test
  public void testCreateWorkerAcceptsReplenishers() {  
    assertFalse(dir.containsKey("Francine")); // ensure that the Worker we will add is not present
    sim.createWorker("Francine", "Replenisher");    
    dir = sim.getDirectory();
    assertTrue(dir.containsKey("Francine"));  // ensure createWorker added this new Worker
    assertTrue(dir.get("Francine") instanceof Replenisher);
  }

  @Test
  public void testCreateWorkerRejectsIncorrectJobs() {
    sim.createWorker("Bertha", "Busdriver");
    dir = sim.getDirectory();
    assertFalse(dir.containsKey("Bertha"));
  }


  @Test
  public void noSpacesInCommandTest() {
    assertTrue(sim.isValidCommand("Loader Larry loads"));
    assertFalse(sim.isValidCommand("LoaderLarry loads"));
    assertFalse(sim.isValidCommand("Loader Larryloads"));
    assertFalse(sim.isValidCommand("LoaderLarryloads"));
  }
  
  @Test
  public void invalidCommandLogsWarningTest() {
    logbackVerifier.expectMessage(Level.WARN);
    sim.parseCommand("Order SES blue red");
  }
  
  @Test
  public void testParsePickerCommandSku() {
    sim.addWorker("Alice", mockPicker);
    when(mockPicker.getType()).thenReturn("Picker");
    sim.parseCommand("Picker Alice picks 37");
    verify(mockPicker).pick("37"); 
  }
  
  @Test
  public void testParsePickerCommandMarshaling() {
    sim.addWorker("Alice", mockPicker);
    when(mockPicker.getType()).thenReturn("Picker");
    sim.parseCommand("Picker Alice to Marshaling");   
    verify(mockPicker).sendNextRequest(); 
  }
  
  @Test
  public void testParsePickerCommandInvalid() {
    sim.addWorker("Alice", mockPicker);
    when(mockPicker.getType()).thenReturn("Picker");
    logbackVerifier.expectMessage(Level.WARN);
    sim.parseCommand("Picker Alice do something not matched by regex"); 
  }
  
  @Test
  public void testParseSequencerCommandSequences() {
    sim.addWorker("Sue", mockSequencer);
    when(mockSequencer.getType()).thenReturn("Sequencer");
    sim.parseCommand("Sequencer Sue sequences");
    verify(mockSequencer).check(); 
  }
  
  @Test
  public void testParseSequencerCommandApproves() {
    sim.addWorker("Sue", mockSequencer);
    when(mockSequencer.getType()).thenReturn("Sequencer");
    sim.parseCommand("Sequencer Sue approves");
    verify(mockSequencer).sendNextRequest(); 
  }
  
  @Test
  public void testParseSequencerCommandRejects() {
    sim.addWorker("Sue", mockSequencer);
    when(mockSequencer.getType()).thenReturn("Sequencer");
    sim.parseCommand("Sequencer Sue rejects");
    verify(mockSequencer).sendToRepick(); 
  }
  
  @Test
  public void testParseSequencerCommandRescans() {
    sim.addWorker("Sue", mockSequencer);
    when(mockSequencer.getType()).thenReturn("Sequencer");
    sim.parseCommand("Sequencer Sue rescans");
    verify(mockSequencer).rescan(); 
  }
  
  @Test
  public void testParseSequencerCommandInvalid() {
    sim.addWorker("Sue", mockSequencer);
    when(mockSequencer.getType()).thenReturn("Sequencer");
    logbackVerifier.expectMessage(Level.WARN);
    sim.parseCommand("Sequencer Sue eats a burger"); 
  }
  
  @Test
  public void testParseLoaderCommandSequences() {
    sim.addWorker("Larry", mockLoader);
    when(mockLoader.getType()).thenReturn("Loader");
    sim.parseCommand("Loader Larry loads");
    verify(mockLoader).check(); 
  }
  
  @Test
  public void testParseLoaderCommandApproves() {
    sim.addWorker("Larry", mockLoader);
    when(mockLoader.getType()).thenReturn("Loader");
    sim.parseCommand("Loader Larry approves");
    verify(mockLoader).sendNextRequest(); 
  }
  
  @Test
  public void testParseLoaderCommandRejects() {
    sim.addWorker("Larry", mockLoader);
    when(mockLoader.getType()).thenReturn("Loader");
    sim.parseCommand("Loader Larry rejects");
    verify(mockLoader).sendToRepick(); 
  }
  
  @Test
  public void testParseLoaderCommandRescans() {
    sim.addWorker("Larry", mockLoader);
    when(mockLoader.getType()).thenReturn("Loader");
    sim.parseCommand("Loader Larry rescans");
    verify(mockLoader).rescan(); 
  }
  
  @Test
  public void testParseLoaderCommandInvalid() {
    sim.addWorker("Larry", mockLoader);
    when(mockLoader.getType()).thenReturn("Loader");
    logbackVerifier.expectMessage(Level.WARN);
    sim.parseCommand("Loader Larry takes a nap"); 
  }
  
  @Test
  public void testParseReplenisherCommandReplenishes() {
    sim.addWorker("Ruby", mockReplenisher);
    when(mockReplenisher.getType()).thenReturn("Replenisher");
    sim.parseCommand("Replenisher Ruby replenishes");
    verify(mockReplenisher).replenish();
  }
  
  @Test
  public void testParseReplenisherCommandInvalid() {
    sim.addWorker("Ruby", mockReplenisher);
    when(mockReplenisher.getType()).thenReturn("Replenisher");
    logbackVerifier.expectMessage(Level.WARN);
    sim.parseCommand("Replenisher Ruby makes a personal call on company time");
  }
  
  @Test
  public void testRunSimulationValidCommand() throws IOException {
    ArrayList<String> commands = new ArrayList<String>();
    commands.add("Order SES Blue");
    sim.runSimulation(commands);
    sim.parseCommand("Order SES Blue");
  }
  
  @Test
  public void testRunSimulationInvalidCommand() throws IOException {
    ArrayList<String> commands = new ArrayList<String>();
    commands.add("Order New Colour");
    sim.runSimulation(commands);
    logbackVerifier.expectMessage(Level.WARN);
  }
  
  @Test
  public void testSimulator() throws FileNotFoundException {
    WorkerManager wm = new WorkerManager();
    RequestManager rm = new RequestManager(wm);;
    OrderManager om = new OrderManager("translation.csv", rm);
    Simulator simulator = new Simulator(rm, om, imMock);
    Worker worker = new Picker("02", rm, imMock);
    simulator.addWorker("02", worker);
    assertSame(simulator.getDirectory().get("02"), worker);
  }
  
  
  @Test
  public void testParseCommandWorkerDne() {
    sim.parseCommand("Picker Rock ready");
    assertTrue(sim.getDirectory().get("Rock").getIsAvail());
  }
  
  @Test
  public void testParseCommandWorkerExist() {
    sim.parseCommand("Picker Rock ready");
    sim.parseCommand("Picker Rock ready");
    assertTrue(sim.getDirectory().get("Rock").getIsAvail());
  }
  
  @Test
  public void testParseWorkerCommandWorkerHasNoTask() {
    sim.addWorker("Alice", mockPicker);
    when(mockPicker.getIsAvail()).thenReturn(true);
    logbackVerifier.expectMessage(Level.WARN);
    sim.parseCommand("Picker Alice to Marshalling");
  }
}

