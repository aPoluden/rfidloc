package lt.vu.mif.rfidloc;

import lt.vu.mif.rfidloc.device.Controller;
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
     * Check that Tag sends TRACK info and Receiver DOES NOT receive it
     * as Receiver does not have PATH defined...
     */
    @Test
    public void singleTagReceiverTest() {
        Network net = new Network(1);

        Tag t = new Tag(net, 0, 0);
        t.start();

        Receiver r = new Receiver(net, 0, 0);
        r.add((m, receiving) -> {

            fail("Receiver should not be sending nor receiving anything here!");

        });
        r.start();

        wait(5);

    }

    /**
     * Check that Tag sends TRACK info and Controller receives it
     */
    @Test
    public void singleTagControllerTest() {
        Network net = new Network(1);

        Tag t = new Tag(net, 0, 0);
        t.start();

        BooleanHolder received = new BooleanHolder();
        Controller c = new Controller(net, 0, 0);
        c.add((m, receiving) -> {

            if (receiving) {
                assertEquals(Operation.TRACK, m.getOp());
                assertEquals(t.getId(), m.getSource());
                assertTrue(m.getStrength() >= 1 && m.getStrength() <= 3);
                assertEquals(0, m.getTarget());
                assertEquals(0, m.getRec());
                assertEquals(0, m.getRecrf());
                assertTrue(m.getRf() > 0);
                received.set();
            } else {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(c.getId(), m.getSource());
                assertEquals(0, m.getStrength());
                assertEquals(0, m.getTarget());
            }

        });
        c.start();

        wait(5);

        assertTrue("Controller has not received messages!", received.get());

    }

    /**
     * Check that Controller sends PATH info and Receiver receives it
     * and Receiver starts to transfer PATH as well
     */
    @Test
    public void singleReceiverControllerTest() {
        Network net = new Network(1);

        Controller c = new Controller(net, 0, 0);
        c.add((m, receiving) -> {

            if (receiving) {
                fail("Controller should not be receiving anything here!");
            } else {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(c.getId(), m.getSource());
                assertEquals(0, m.getStrength());
                assertEquals(0, m.getTarget());
            }

        });
        c.start();

        BooleanHolder received = new BooleanHolder();
        BooleanHolder sent = new BooleanHolder();

        Receiver r = new Receiver(net, 0, 0);
        r.add((m, receiving) -> {

            if (receiving) {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(c.getId(), m.getSource());
                assertEquals(0, m.getStrength());
                assertEquals(0, m.getTarget());
                assertEquals(0, m.getRec());
                assertEquals(0, m.getRecrf());
                assertTrue(m.getRf() > 0);
                received.set();
            } else {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(r.getId(), m.getSource());
                assertEquals(1, m.getStrength());
                assertEquals(0, m.getTarget());
                sent.set();
            }

        });
        r.start();

        wait(10);

        assertTrue("Receiver has not received messages!", received.get());
        assertTrue("Receiver has not sent messages!", sent.get());

    }

}
