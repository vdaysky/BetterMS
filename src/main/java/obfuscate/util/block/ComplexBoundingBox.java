package obfuscate.util.block;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.Arrays;

public class ComplexBoundingBox {

    private ArrayList<BoundingBox> boxes;

    public ComplexBoundingBox(ArrayList<BoundingBox> boxes) {
        this.boxes = boxes;
    }

    public boolean isInside(double x, double y, double z) {
        for (BoundingBox box : boxes) {
            if (box.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean isInside(Location loc) {
        return isInside(loc.getX(), loc.getY(), loc.getZ());
    }

    public ArrayList<ArrayList<ArrayList<Double>>> toObject() {
        ArrayList<ArrayList<ArrayList<Double>>> ret = new ArrayList<>();
        for (BoundingBox box : boxes) {
            ret.add(new ArrayList<>(Arrays.asList(
                    new ArrayList<>(Arrays.asList(box.getMinX(), box.getMinY(), box.getMinZ())),
                    new ArrayList<>(Arrays.asList(box.getMaxX(), box.getMaxY(), box.getMaxZ()))
            )));
        }
        return ret;
    }

    public ArrayList<BoundingBox> getBoxes() {
        return boxes;
    }
}
