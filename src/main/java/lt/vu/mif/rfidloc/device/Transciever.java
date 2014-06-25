package lt.vu.mif.rfidloc.device;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static lt.vu.mif.rfidloc.device.Device.wait;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.network.Network;

public abstract class Transciever extends Device {

    private static final int SEND_POWER = 255;
    
    private final Random rand = new Random();
    protected final BlockingQueue<Message> sending = new LinkedBlockingQueue<>();
    
    public Transciever(Network net, int x, int y) {
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
    protected void init() {
        receiving.set(Boolean.TRUE);
        sending.clear();
    }
    
    @Override
    protected void deinit() {
        sending.clear();
    }
    
    @Override
    protected Runnable[] getTasks() {
        return new Runnable[] { receiverTask(), senderTask(), pathTask() };
    }

}
