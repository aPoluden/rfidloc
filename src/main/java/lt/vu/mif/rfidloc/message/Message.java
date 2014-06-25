package lt.vu.mif.rfidloc.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class Message implements Cloneable {

    private static long IDSEQ = 0;
    
    // Identifier
    private final long id = ++IDSEQ;
    
    // Operation type
    private final Operation op;
    
    // TRACK: signal strength
    // PATH: path length
    private final int strength;
    
    // Source of the message
    private final int source;
    
    // TRACK: target Receiver
    @Setter
    private int target = 0;

    // TRACK: first Receiver
    @Setter
    private int rec = 0;
    
    // TRACK: first Receiver rf
    @Setter
    private int recrf = 0;
    
    // Received signal strength
    @Setter
    private int rf = 0;

    @Override
    public Message clone() {
        try {
            return (Message) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
    
}
