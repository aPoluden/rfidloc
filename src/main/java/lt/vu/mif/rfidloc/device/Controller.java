package lt.vu.mif.rfidloc.device;

import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.message.Operation;
import lt.vu.mif.rfidloc.network.Network;

@Log4j
@ToString(callSuper = true)
public class Controller extends Transciever {

    public Controller(Network net, int x, int y) {
        super(net, x, y);
    }

    @Override
    protected void processMessage(Message m) {
        if (m.getOp() == Operation.TRACK) {
            if (m.getTarget() == 0 || m.getTarget() == getId()) {
                log.info(m);
            }
        }
    }
    
    @Override
    protected Message getPathMessage() {
        return new Message(Operation.PATH, 0, getId());
    }

}
