package lt.vu.mif.rfidloc.device;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.vu.mif.rfidloc.listener.MessageListener;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.network.Network;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Log4j
@ToString(exclude = { "messages", "service" })
@EqualsAndHashCode(of = "id")
public abstract class Device {

    private static final int SLEEP_AFTER_SEND_MS = 100;
    private static int ID_SEQ = 0;

    @Getter
    private final Network net;
    
    @Getter
    private final int id = ++ID_SEQ;

    @Getter
    private final Coords coords;

    private final AtomicBoolean running = new AtomicBoolean(Boolean.FALSE);
    protected final AtomicBoolean receiving = new AtomicBoolean(Boolean.TRUE);
    protected final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private ExecutorService service;
    
    public Device(Network net, Coords coords) {
        this.net = net;
        this.coords = coords;
        net.add(this);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Constructed: %s", this));
        }
    }
    
    public void receive(Message m) {
        if (running.get() && receiving.get()) {
            messages.add(m);
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s %d received: %s", this.getClass().getSimpleName(), id, m));
            }
        }
    }
    
    protected void send(Message m, int strength) {
        if (running.get()) {
            boolean prev = this.receiving.getAndSet(Boolean.FALSE);
            this.net.send(this, m, strength);

            listenSending(m);

            if (log.isDebugEnabled()) {
                log.debug(String.format("%s %d sent: %s", this.getClass().getSimpleName(), id, m));
            }

            try {
                Thread.sleep(SLEEP_AFTER_SEND_MS);
            } catch (InterruptedException ex) { }

            this.receiving.set(prev);
        }
    }
   
    protected abstract void setup();
    protected abstract void tearDown();
    protected abstract Runnable[] getTasks();
    
    public void start() {
        Runnable[] tasks = getTasks();
        setup();
        if (tasks.length > 0) {
            service = Executors.newFixedThreadPool(tasks.length);
            Stream.of(tasks).forEach(t -> service.submit(t));
        }
        running.set(Boolean.TRUE);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Started: %s", this));
        }
    }

    public void stop() {
        this.running.set(Boolean.FALSE);
        if (service != null) {
            service.shutdownNow();
            service = null;
        }
        this.messages.clear();
        tearDown();
        if (log.isDebugEnabled()) {
            log.debug(String.format("Stopped: %s", this));
        }
    }
    
    public void reset() {
        if (this.running.get()) {
            this.stop();
            this.start();
        }
    }
    
    protected static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) { }
    }

    private final Set<MessageListener> listeners = Collections.synchronizedSet(new HashSet<>());

    public void add(MessageListener listener) {
        listeners.add(listener);
    }

    public void remove(MessageListener listener) {
        listeners.remove(listener);
    }

    public void clearMessageListeners() {
        listeners.clear();
    }

    protected void listenReceiving(Message m) {
        listeners.parallelStream().forEach(l -> l.process(m, Boolean.TRUE));
    }

    protected void listenSending(Message m) {
        listeners.parallelStream().forEach(l -> l.process(m, Boolean.FALSE));
    }

}