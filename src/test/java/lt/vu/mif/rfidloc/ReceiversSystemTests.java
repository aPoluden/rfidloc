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
public class ReceiversSystemTests extends TestBase {

    /**
     * Check that Controller setups AWSN
     */
    @Test
    public void twoReceiversTest() {
        Network net = new Network(1);

        BooleanHolder r2_setup = new BooleanHolder();
        BooleanHolder r3_setup = new BooleanHolder();

        Controller c = new Controller(net, 0, 0);
        Receiver r1 = new Receiver(net, 150, 150);
        Receiver r2 = new Receiver(net, 300, 300);

        r1.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(r1.getId(), m.getSource());
                assertEquals(1, m.getStrength());
                assertEquals(0, m.getTarget());
                r2_setup.set();
            }
        });

        r2.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(r2.getId(), m.getSource());
                assertEquals(2, m.getStrength());
                assertEquals(0, m.getTarget());
                r3_setup.set();
            }
        });

        c.start();
        r1.start();
        r2.start();

        wait(30);

        assertTrue("Receiver 2 was not setup correctly!", r2_setup.get());
        assertTrue("Receiver 3 was not setup correctly!", r3_setup.get());

    }

    /**
     * Check that Controller setups AWSN
     */
    @Test
    public void multipleReceiversTest() {
        Network net = new Network(1);

        BooleanHolder r2_setup = new BooleanHolder();
        BooleanHolder r3_setup = new BooleanHolder();
        BooleanHolder r4_setup = new BooleanHolder();
        BooleanHolder r5_setup = new BooleanHolder();

        Controller c = new Controller(net, 0, 0);
        Receiver r1 = new Receiver(net, 150, 150);
        Receiver r2 = new Receiver(net, 300, 300);
        Receiver r3 = new Receiver(net, 450, 450);
        Receiver r4 = new Receiver(net, 600, 600);

        r1.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(r1.getId(), m.getSource());
                assertEquals(1, m.getStrength());
                assertEquals(0, m.getTarget());
                r2_setup.set();
            }
        });

        r2.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(r2.getId(), m.getSource());
                assertEquals(2, m.getStrength());
                assertEquals(0, m.getTarget());
                r3_setup.set();
            }
        });

        r3.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(r3.getId(), m.getSource());
                assertEquals(3, m.getStrength());
                assertEquals(0, m.getTarget());
                r4_setup.set();
            }
        });

        r4.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(Operation.PATH, m.getOp());
                assertEquals(r4.getId(), m.getSource());
                assertEquals(4, m.getStrength());
                assertEquals(0, m.getTarget());
                r5_setup.set();
            }
        });

        c.start();
        r1.start();
        r2.start();
        r3.start();
        r4.start();

        wait(30);

        assertTrue("Receiver 2 was not setup correctly!", r2_setup.get());
        assertTrue("Receiver 3 was not setup correctly!", r3_setup.get());
        assertTrue("Receiver 4 was not setup correctly!", r4_setup.get());
        assertTrue("Receiver 5 was not setup correctly!", r5_setup.get());

    }

    /**
     * Check that second Controller re-setups Receivers
     */
    @Test
    public void secondControllerTest() {
        Network net = new Network(1);

        Controller c1 = new Controller(net, 0, 0);
        Receiver r1 = new Receiver(net, 150, 150);
        Receiver r2 = new Receiver(net, 300, 300);
        Receiver r3 = new Receiver(net, 450, 450);
        Receiver r4 = new Receiver(net, 600, 600);

        c1.start();
        r1.start();
        r2.start();
        r3.start();
        r4.start();

        r4.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(4, m.getStrength());
            }
        });

        wait(30);

        r4.clearMessageListeners();

        Controller c2 = new Controller(net, 750, 750);
        c2.start();

        wait(15);

        r4.add((m, receiving) -> {
            if (!receiving) {
                assertEquals(1, m.getStrength());
            }
        });

    }

}
