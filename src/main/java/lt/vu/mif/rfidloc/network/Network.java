package lt.vu.mif.rfidloc.network;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j;
import lt.vu.mif.rfidloc.device.Device;
import lt.vu.mif.rfidloc.message.Message;

import java.util.HashSet;
import java.util.Set;

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
                int dist = sender.getCoords().getDistTo(d.getCoords());
                if (strength > dist) {
                    Message m1 = m.clone();
                    m1.setRf(strength - dist);
                    d.receive(m1);
                }
            });
    }

}
