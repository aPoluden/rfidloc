package lt.vu.mif.rfidloc.device;

import lombok.ToString;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ToString(callSuper = true)
public class Tag extends Device {

    private static final List<Integer> strengthList = new ArrayList<>();
    static {
        strengthList.add(85);
        strengthList.add(170);
        strengthList.add(255);
    }
    
    private final Random rand = new Random();

    public Tag(Network net) {
        this(net, Coords.build(0, 0, 0));
    }

    public Tag(Network net, Coords coords) {
        super(net, coords);
    }
    
    @Override
    protected void setup() {
        this.receiving.set(Boolean.FALSE);
    }

    @Override
    protected Runnable[] getTasks() {
        return new Runnable[] { () -> {
            
            while (true) {
                for (int si = 0; si < strengthList.size(); si++) {

                    Message m = new Message(Operation.TRACK, si + 1, getId());
                    send(m, strengthList.get(si));
                    
                    wait(100 * (5 + rand.nextInt(5)));

                }
            }
            
        } };
    }

    @Override
    protected void tearDown() { }

}
