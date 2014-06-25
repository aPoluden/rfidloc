package lt.vu.mif.rfidloc.device;

import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.network.Network;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Transceiver extends Device {

    private static final int SEND_POWER = 255;
    
    private final Random rand = new Random();
    protected final BlockingQueue<Message> sending = new LinkedBlockingQueue<>();
    
    public Transceiver(Network net, int x, int y) {
        super(net, x, y);
    }

    protected abstract void processMessage(Message m);
    protected abstract Message getPathMessage();
    
    protected Runnable receiverTask() {
        return () -> {
            try {
                while (true) {
                    processMessage(messages.take());
                    wait(rand.nextInt(100));
                }
            } catch (InterruptedException ex) { }
        };
    }
    
    protected Runnable senderTask() {
        return () -> {
            try {
                while (true) {
                    send(sending.take(), SEND_POWER);
                    wait(rand.nextInt(100));
                }
            } catch (InterruptedException ex) { }
        };
    }
    
    protected Runnable pathTask() {
        return () -> {
            while (true) {
                Message m = getPathMessage();
                if (m != null) {
                    sending.add(m);
                }
                wait(1000 * rand.nextInt(10));
            }
        };
    }
    
    @Override
    protected void setup() {
        receiving.set(Boolean.TRUE);
        sending.clear();
    }
    
    @Override
    protected void tearDown() {
        sending.clear();
    }
    
    @Override
    protected Runnable[] getTasks() {
        return new Runnable[] { receiverTask(), senderTask(), pathTask() };
    }

}
