package lt.vu.mif.rfidloc.device;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.ToString;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;

@ToString(callSuper = true)
public class Receiver extends Transceiver {
    
    private static final int PATH_COUNT_DOWN = 10;
    
    private final AtomicInteger pathLen = new AtomicInteger(0);
    private final AtomicInteger pathTarget = new AtomicInteger(0);
    private final AtomicInteger pathCounter = new AtomicInteger(0);
    
    public Receiver(Network net, int x, int y) {
        super(net, x, y);
    }
    
    @Override
    protected void processMessage(Message m) {
        switch (m.getOp()) {
            case TRACK:
                if (pathCounter.get() > 0) {
                    listenTo(m, true);
                    m = m.clone();
                    if (m.getTarget() == 0) {
                        m.setRec(getId());
                        m.setRecrf(m.getRf());
                    } else if (m.getTarget() != getId()) {
                        return;
                    }
                    m.setTarget(pathTarget.get());
                    sending.add(m);
                }
                break;
            case PATH:
                if (pathCounter.get() == 0 || pathLen.get() >= m.getStrength()) {
                    listenTo(m, true);
                    pathLen.set(m.getStrength());
                    pathTarget.set(m.getSource());
                    pathCounter.set(PATH_COUNT_DOWN);
                }
                break;
        }
    }

    @Override
    protected Message getPathMessage() {
        if (pathTarget.get() > 0 && pathCounter.getAndDecrement() > 0) {
            return new Message(Operation.PATH, pathLen.get() + 1, getId());
        } else {
            return null;
        }
    }
    
}
