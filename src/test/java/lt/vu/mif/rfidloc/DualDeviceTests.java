package lt.vu.mif.rfidloc;

import lt.vu.mif.rfidloc.device.Receiver;
import lt.vu.mif.rfidloc.device.Tag;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DualDeviceTests extends TestBase {

    /**
     * Check that Tag sends TRACK info and Receiver receives it
     */
    @Test
    public void singleTagTest() {
        Network net = new Network(1);

        Tag t = new Tag(net, 0, 0);
        t.add(m -> {

            assertEquals(Operation.TRACK, m.getOp());
            assertEquals(t.getId(), m.getSource());
            assertTrue(m.getStrength() >= 1 && m.getStrength() <= 3);
            assertEquals(0, m.getTarget());
            assertEquals(0, m.getRec());
            assertEquals(0, m.getRecrf());
            assertEquals(0, m.getRf());

        }, Boolean.FALSE);
        t.start();

        Receiver r = new Receiver(net, 0, 0);
        r.add(m -> {

            fail("Receiver should not be send anything!");

        }, Boolean.FALSE);
        r.add(m -> {

            assertEquals(Operation.TRACK, m.getOp());
            assertEquals(t.getId(), m.getSource());
            assertTrue(m.getStrength() >= 1 && m.getStrength() <= 3);
            assertEquals(0, m.getTarget());
            assertEquals(0, m.getRec());
            assertEquals(0, m.getRecrf());
            assertTrue(m.getRf() > 0);

        }, Boolean.FALSE);
        r.start();

        wait(5);

    }
    
}
