package project;

import java.util.ArrayDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkerManager {

  private ArrayDeque<Picker> availablePickers;
  private ArrayDeque<Sequencer> availableSequencers;
  private ArrayDeque<Loader> availableLoaders;
  private ArrayDeque<Replenisher> availableReplenishers;
  private static final Logger logger = LoggerFactory.getLogger(WorkerManager.class);

  /**
   * Initialize a WorkerManager that contains 4 ArrayDeques, availablePickers, 
   * availableSequencers, availableLoaders, availableReplenishers.
   */
  public WorkerManager() {
    this.availablePickers = new ArrayDeque<>();
    this.availableSequencers = new ArrayDeque<>();
    this.availableLoaders = new ArrayDeque<>();
    this.availableReplenishers = new ArrayDeque<>();
  }

  /**
   * Add a worker to the ArrayDeque queue they belong in.
   * @param w A worker to be enqueued.
   */
  void enqueueWorker(Worker w) {
    if (w instanceof Picker) {
      availablePickers.add((Picker) w);
    } else if (w instanceof Sequencer) {
      availableSequencers.add((Sequencer) w);
    } else if (w instanceof Loader) {
      availableLoaders.add((Loader) w);
    } else if (w instanceof Replenisher) {
      availableReplenishers.add((Replenisher) w);
    } else {
      logger.info(String.format("Worker %s could not be queued since they are not "
          + "a Picker, Sequencer, Loader, or Replenisher", w.identity));
      return;
    }
    
    logger.info(String.format("%s %s is placed in the %s queue",
                                         w.getType(), w.identity, w.getType()));
  }

  Picker nextPicker() {
    return availablePickers.poll();
  }

  Sequencer nextSequencer() {
    return availableSequencers.poll();
  }

  Loader nextLoader() {
    return availableLoaders.poll();
  }

  Replenisher nextReplenisher() {
    return availableReplenishers.poll();
  }
}
