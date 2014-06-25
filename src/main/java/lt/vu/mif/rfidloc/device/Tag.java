package lt.vu.mif.rfidloc.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.ToString;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;

@ToString(callSuper = true)
public class Tag extends Device {

    private static final List<Integer> strengthList = new ArrayList<>();
    static {
        strengthList.add(85);
        strengthList.add(170);
        strengthList.add(255);
    }
    
    private final Random rand = new Random();
    
    public Tag(Network net, int x, int y) {
        super(net, x, y);
    }
    
    @Override
    protected void init() {
        this.receiving.set(Boolean.FALSE);
    }

    @Override
    protected Runnable[] getTasks() {
        return new Runnable[] { () -> {
            
            while (true) {
                for (int si = 0; si < strengthList.size(); si++) {

                    Message m = new Message(Operation.TRACK, si + 1, getId());
                    send(m, strengthList.get(si));
                    
                    wait(50 * rand.nextInt(25));

                }
            }
            
        } };
    }

    @Override
    protected void deinit() {
        
    }

}
