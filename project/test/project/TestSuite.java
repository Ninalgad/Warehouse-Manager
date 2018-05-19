package project;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
    CheckerWorkerTest.class,
    InventoryManagerTest.class,
    LoaderTest.class,    
    OrderManagerTest.class,
    OrderTest.class,
    PickerTest.class,
    PickingRequestTest.class,
    ReplenisherTest.class,
    RequestManagerTest.class,
    SequencerTest.class,
    SimulatorTest.class,
    WarehousePickingTest.class,
    WorkerManagerTest.class,
    WorkerTest.class,
})

public class TestSuite {   
}  