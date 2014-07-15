package lt.vu.mif.rfidloc.device;

import lombok.ToString;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;

import java.util.concurrent.atomic.AtomicInteger;

@ToString(callSuper = true)
public class Receiver extends Transceiver {
    
    private static final int PATH_COUNT_DOWN = 10;
    
    private final AtomicInteger pathLen = new AtomicInteger(0);
    private final AtomicInteger pathTarget = new AtomicInteger(0);
    private final AtomicInteger pathCounter = new AtomicInteger(0);

    public Receiver(Network net) {
        this(net, Coords.build(0, 0, 0));
    }

    public Receiver(Network net, Coords coords) {
        super(net, coords);
    }
    
    @Override
    protected void processMessage(Message m) {
        switch (m.getOp()) {
            case TRACK:
                if (pathCounter.get() > 0) {
                    Message ms = m.clone();
                    if (ms.getTarget() == 0) {
                        ms.setRec(getId());
                        ms.setRecrf(ms.getRf());
                    } else if (ms.getTarget() != getId()) {
                        return;
                    }
                    listenReceiving(m);
                    ms.setTarget(pathTarget.get());
                    sending.add(ms);
                }
                break;
            case PATH:
                if (pathCounter.get() == 0 || pathLen.get() >= m.getStrength()) {
                    listenReceiving(m);
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
