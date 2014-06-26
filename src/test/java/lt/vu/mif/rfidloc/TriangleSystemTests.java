package lt.vu.mif.rfidloc;

import lt.vu.mif.rfidloc.device.Controller;
import lt.vu.mif.rfidloc.device.Receiver;
import lt.vu.mif.rfidloc.device.Tag;
import lt.vu.mif.rfidloc.listener.LocationListener;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class TriangleSystemTests extends TestBase {

    /**
     * Check that Tag info reaches Controller from 3 Receivers
     * via single Receiver. Receivers are in Triangle
     */
    @Test
    public void triangleTest() {
        Network net = new Network(1);

        BooleanHolder r1_data = new BooleanHolder();
        BooleanHolder r2_data = new BooleanHolder();
        BooleanHolder r3_data = new BooleanHolder();

        Controller c = new Controller(net, 0, 0);
        Receiver r1 = new Receiver(net, 150, 150);
        Receiver r2 = new Receiver(net, 150, 300);
        Receiver r3 = new Receiver(net, 300, 150);
        Tag t = new Tag(net, 225, 225);

        c.add((tag, rec, strength, rf) -> {
            assertEquals(t.getId(), tag);
            if (rec == r1.getId()) r1_data.set();
            if (rec == r2.getId()) r2_data.set();
            if (rec == r3.getId()) r3_data.set();
        });

        c.start();
        r1.start();
        r2.start();
        r3.start();
        t.start();

        wait(30);

        assertTrue("Controller 1 does not receive data from Receiver 2!", r1_data.get());
        assertTrue("Controller 1 does not receive data from Receiver 3!", r2_data.get());
        assertTrue("Controller 1 does not receive data from Receiver 4!", r3_data.get());

    }

}
