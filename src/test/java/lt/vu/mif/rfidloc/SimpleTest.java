package lt.vu.mif.rfidloc;

import lt.vu.mif.rfidloc.device.Controller;
import lt.vu.mif.rfidloc.device.Receiver;
import lt.vu.mif.rfidloc.device.Tag;
import lt.vu.mif.rfidloc.network.Network;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimpleTest {

    @Test
    public void tagTest() {
        Network net = new Network(1);
        
        Controller con = new Controller(net, 0, 0);
        con.start();
        
        Receiver rec1 = new Receiver(net, 150, 150);
        rec1.start();
        
        Receiver rec2 = new Receiver(net, 300, 300);
        rec2.start();
        
        Tag tag1 = new Tag(net, 350, 350);
        tag1.start();

        Tag tag2 = new Tag(net, 225, 225);
        tag2.start();
        
        wait(60);
        
        con.start();
        
        wait(5);
        
    }
    
    private void wait(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException ex) { }
    }
    
    
}
