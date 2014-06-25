package lt.vu.mif.rfidloc;

public class TestBase {

    protected static void wait(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException ex) { }
    }

}
