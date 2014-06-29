package lt.vu.mif.rfidloc;

import lt.vu.mif.rfidloc.device.Controller;
import lt.vu.mif.rfidloc.device.Coords;
import lt.vu.mif.rfidloc.device.Receiver;
import lt.vu.mif.rfidloc.device.Tag;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SingleDeviceTests extends TestBase {

    /**
     * Check that Tag sends TRACK info
     */
    @Test
    public void singleTagTest() {
        Network net = new Network(1);
        
        Tag t = new Tag(net);
        t.add((m, receiving) -> {

            if (receiving) {
                fail("Tag should not receive anything!");
            } else {
                assertEquals(Operation.TRACK, m.getOp());
                assertEquals(t.getId(), m.getSource());
                assertTrue(m.getStrength() >= 1 && m.getStrength() <= 3);
            }

        });
        t.start();

        wait(5);
    }

    /**
     * Check that Controller sends PATH info
     */
    @Test
    public void singleControllerTest() {
        Network net = new Network(1);

        Controller c = new Controller(net);
        c.add((m, receiving) -> {

            if (receiving) {
                fail("Controller should not receive anything as there is nothing else in the network!");
            } else {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(c.getId(), m.getSource());
                assertEquals(0, m.getStrength());
                assertEquals(0, m.getTarget());
            }

        });
        c.start();

        wait(5);
    }

    /**
     * Check that Receiver does not send anything...
     */
    @Test
    public void singleReceiverTest() {
        Network net = new Network(1);

        Receiver r = new Receiver(net);
        r.add((m, receiving) -> {

            fail("Receiver should not be sending nor receiving anything!");

        });
        r.start();

        wait(5);
    }
    
}
