package lt.vu.mif.rfidloc;

public class TestBase {

    protected static void wait(int secs) {
        try {
            Thread.sleep(secs * 1000);
        } catch (InterruptedException ex) { }
    }

    protected class BooleanHolder {

        private boolean value = Boolean.FALSE;

        public void set() {
            this.value = Boolean.TRUE;
        }

        public void reset() {
            this.value = Boolean.FALSE;
        }

        public boolean get() {
            return this.value;
        }

    }

}
