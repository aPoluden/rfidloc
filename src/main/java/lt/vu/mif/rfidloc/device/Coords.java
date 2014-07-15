package lt.vu.mif.rfidloc.device;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class Coords {

    private int x;
    private int y;
    private int z;

    private Coords(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void moveTo(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void move(int dx, int dy, int dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public int getDistTo(Coords d) {
        return getDist(this, d);
    }

    public static int getDist(Coords d1, Coords d2) {
        int dx = d1.getX() - d2.getX();
        int dy = d1.getY() - d2.getY();
        int dz = d1.getZ() - d2.getZ();
        Double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return dist.intValue();
    }

    public static Coords build(int x, int y, int z) {
        return new Coords(x, y, z);
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("(")
            .append(x)
            .append(",")
            .append(y)
            .append(",")
            .append(z)
            .append(")")
            .toString();
    }

}
