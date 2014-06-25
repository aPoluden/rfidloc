package lt.vu.mif.rfidloc.device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.vu.mif.rfidloc.listener.MessageListener;
import lt.vu.mif.rfidloc.message.Message;
import lt.vu.mif.rfidloc.network.Network;

@Log4j
@ToString(exclude = { "messages", "service" })
@EqualsAndHashCode(of = "id")
public abstract class Device {

    private static final int SLEEP_AFTER_SEND_MS = 100;
    private static int IDSEQ = 0;

    @Getter
    private final Network net;
    
    @Getter
    private final int id = ++IDSEQ;
    
    @Getter
    @Setter
    private int x;
    
    @Getter
    @Setter
    private int y;
    
    private final AtomicBoolean running = new AtomicBoolean(Boolean.FALSE);
    protected final AtomicBoolean receiving = new AtomicBoolean(Boolean.TRUE);
    protected final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private ExecutorService service;
    
    public Device(Network net, int x, int y) {
        this.net = net;
        this.x = x;
        this.y = y;
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
            listenTo(m, Boolean.FALSE);
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s %d sent: %s", this.getClass().getSimpleName(), id, m));
            }

            try {
                Thread.sleep(SLEEP_AFTER_SEND_MS);
            } catch (InterruptedException ex) { }

            this.receiving.set(prev);
        }
    }
   
    protected abstract void init();
    protected abstract void deinit();
    protected abstract Runnable[] getTasks();
    
    public void start() {
        Runnable[] tasks = getTasks();
        init();
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
        deinit();
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
    
    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int dx, int dy) {
        this.x += x;
        this.y += y;
    }
    
    protected static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) { }
    }

    private final Map<MessageListener, Boolean> listeners = new ConcurrentHashMap<>();

    public void add(MessageListener listener, boolean isReceive) {
        listeners.put(listener, isReceive);
    }

    public void remove(MessageListener listener) {
        listeners.remove(listener);
    }

    protected void listenTo(Message m, boolean isReceive) {
        listeners.entrySet().parallelStream()
                .filter(e -> e.getValue() == isReceive)
                .map(e -> e.getKey())
                .forEach(l -> l.process(m));
    }

}