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
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SimpleSystemTests extends TestBase {

    /**
     * Check that Controller receive Tag info directly and from Receiver
     */
    @Test
    public void doTest() {
        Network net = new Network(1);

        BooleanHolder t_received = new BooleanHolder();
        BooleanHolder r_received = new BooleanHolder();

        Tag t = new Tag(net, Coords.build(50, 50, 50));
        Receiver r = new Receiver(net, Coords.build(100, 100, 100));
        Controller c = new Controller(net, Coords.build(0, 0, 0));

        c.add((m, receiving) -> {

            if (receiving) {
                if (m.getOp().equals(Operation.TRACK)) {
                    assertEquals(t.getId(), m.getSource());
                    assertTrue(m.getStrength() >= 1 && m.getStrength() <= 3);
                    if (m.getTarget() == 0) {
                        assertEquals(0, m.getRec());
                        assertEquals(0, m.getRecrf());
                        assertTrue(m.getRf() > 0);
                        t_received.set();
                    } else
                    if (m.getTarget() == c.getId()) {
                        assertEquals(r.getId(), m.getRec());
                        assertTrue(m.getRecrf() > 0);
                        assertTrue(m.getRf() > 0);
                        r_received.set();
                    } else {
                        fail("Wrong target value received!");
                    }
                }
            }

        });

        c.start();
        r.start();
        t.start();


        wait(10);

        assertTrue("Controller has not received direct Tag messages!", t_received.get());
        assertTrue("Controller has not received Receiver Tag messages!", r_received.get());

    }

}
