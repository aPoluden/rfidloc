package lt.vu.mif.rfidloc.device;

import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j
@ToString(callSuper = true)
public class Controller extends Transceiver {

    public Controller(Network net, int x, int y) {
        super(net, x, y);
    }

    @Override
    protected void processMessage(Message m) {
        if (m.getTarget() == 0 || m.getTarget() == getId()) {
            listenTo(m, Boolean.TRUE);
        }
    }
    
    @Override
    protected Message getPathMessage() {
        return new Message(Operation.PATH, 0, getId());
    }

}
