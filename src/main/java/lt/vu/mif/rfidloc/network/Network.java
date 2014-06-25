package lt.vu.mif.rfidloc.network;

import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.vu.mif.rfidloc.device.Device;
import lt.vu.mif.rfidloc.message.Message;

@Log4j
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = { "id" })
public class Network {

    @Getter
    private final int id;
    
    private final Set<Device> devices = new HashSet<>();
    
    public void add(Device d) {
        devices.add(d);
    }
    
    public void send(Device sender, Message m, int strength) {
        devices.stream()
            .filter(d -> !sender.equals(d))
            .forEach(d -> {
                int dist = getDist(sender, d);
                if (strength > dist) {
                    Message m1 = m.clone();
                    m1.setRf(strength - dist);
                    d.receive(m1);
                }
            });
    }

    public static int getDist(Device d1, Device d2) {
        int dx = d1.getX() - d2.getX();
        int dy = d1.getY() - d2.getY();
        Double dist = Math.sqrt(dx * dx + dy * dy);
        return dist.intValue();
    }
    
}
