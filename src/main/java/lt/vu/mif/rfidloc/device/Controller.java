package lt.vu.mif.rfidloc.device;

import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.vu.mif.rfidloc.listener.LocationListener;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Log4j
@ToString(callSuper = true)
public class Controller extends Transceiver {

    public Controller(Network net) {
        this(net, Coords.build(0, 0, 0));
    }

    public Controller(Network net, Coords coords) {
        super(net, coords);
    }

    @Override
    protected void processMessage(Message m) {
        if (m.getOp().equals(Operation.TRACK)) {
            if (m.getTarget() == 0 || m.getTarget() == getId()) {

                listenReceiving(m);

                int rec = m.getTarget() == 0 ? getId() : m.getRec();
                int rf = m.getTarget() == 0 ? m.getRf() : m.getRecrf();

                listeners.parallelStream()
                        .forEach(l -> l.process(m.getSource(), rec, m.getStrength(), rf));

                if (log.isDebugEnabled()) {
                    log.debug(String.format("%s %d location: tag=%d, rec=%d, str=%d, rf=%d",
                            this.getClass().getSimpleName(), getId(), m.getSource(), rec, m.getStrength(), rf));
                }

            }
        }
    }
    
    @Override
    protected Message getPathMessage() {
        return new Message(Operation.PATH, 0, getId());
    }

    private final Set<LocationListener> listeners = Collections.synchronizedSet(new HashSet<>());

    public void add(LocationListener listener) {
        listeners.add(listener);
    }

    public void remove(LocationListener listener) {
        listeners.remove(listener);
    }

}
