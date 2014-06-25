package lt.vu.mif.rfidloc;

import lt.vu.mif.rfidloc.device.Controller;
import lt.vu.mif.rfidloc.device.Receiver;
import lt.vu.mif.rfidloc.device.Tag;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SingleDeviceTests extends TestBase {

    /**
     * Check that Tag sends TRACK info
     */
    @Test
    public void singleTagTest() {
        Network net = new Network(1);
        
        Tag t = new Tag(net, 0, 0);
        t.add(m -> {

            assertEquals(Operation.TRACK, m.getOp());
            assertEquals(t.getId(), m.getSource());
            assertTrue(m.getStrength() >= 1 && m.getStrength() <= 3);

        }, Boolean.FALSE);
        t.start();

        wait(5);
    }

    /**
     * Check that Controller sends PATH info
     */
    @Test
    public void singleControllerTest() {
        Network net = new Network(1);

        Controller c = new Controller(net, 0, 0);
        c.add(m -> {

            assertEquals(Operation.PATH, m.getOp());
            assertEquals(c.getId(), m.getSource());
            assertEquals(0, m.getStrength());
            assertEquals(0, m.getTarget());

        }, Boolean.FALSE);
        c.start();

        wait(5);
    }

    /**
     * Check that Receiver does not send anything...
     */
    @Test
    public void singleReceiverTest() {
        Network net = new Network(1);

        Receiver r = new Receiver(net, 0, 0);
        r.add(m -> {

            fail("Receiver should not be send anything!");

        }, Boolean.FALSE);
        r.start();

        wait(5);
    }
    
}
