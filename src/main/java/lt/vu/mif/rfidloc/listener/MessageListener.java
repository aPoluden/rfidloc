package lt.vu.mif.rfidloc.listener;

import lt.vu.mif.rfidloc.message.Message;

@FunctionalInterface
public interface MessageListener {

    public void process(Message m);

}
