package lt.vu.mif.rfidloc.listener;

@FunctionalInterface
public interface LocationListener {

    public void process(int tag, int rec, int strength, int rf);

}
