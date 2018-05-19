package project;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class PickingRequestTest {
  PickingRequest req;
  
  @Before
  public void initialize() {
    req = new PickingRequest(1);
  }
  
  @Test
  public void testUpdateFasciaState() {
    int initialSize = req.getFasciaState().size();
    req.updateFasciaState("sku");
    int finalSize = req.getFasciaState().size();
    assertEquals(initialSize + 1, finalSize);
  }

  @Test
  public void testClearFasciaState() {
    req.clearFasciaState();
    int finalSize = req.getFasciaState().size();
    assertEquals(0, finalSize);
  }

}
