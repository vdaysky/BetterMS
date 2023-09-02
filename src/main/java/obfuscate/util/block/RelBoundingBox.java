package obfuscate.util.block;


import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class RelBoundingBox extends BoundingBox {

    public RelBoundingBox(Vector min, Vector max) {
        super(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    public RelBoundingBox(Location center, double x, double y, double z) {
        super(
            center.getX() - x, center.getY() - y, center.getZ() - z,
            center.getX() + x, center.getY() + y, center.getZ() + z
        );
    }

    public RelBoundingBox(Vector center, double x, double y, double z) {
        super(
                center.getX() - x, center.getY() - y, center.getZ() - z,
                center.getX() + x, center.getY() + y, center.getZ() + z
        );
    }

    public RelBoundingBox(double x, double y, double z, double x2, double y2, double z2) {
        super(x, y, z, x2, y2, z2);
    }

    public boolean isInside(double x, double y, double z) {
        return this.contains(new Vector(x, y, z));
    }

    public boolean isInside(Location loc) {
        return isInside(loc.getX(), loc.getY(), loc.getZ());
    }

}
