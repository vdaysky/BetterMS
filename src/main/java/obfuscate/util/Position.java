package obfuscate.util;

import org.bukkit.Location;
import org.bukkit.World;

public class Position
{
    double x, y, z;
    double pitch, yaw;


    public Position(Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.pitch = loc.getPitch();
        this.yaw = loc.getYaw();
    }
    public Position(double  x, double y, double z, double pitch, double yaw)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }
    public Location toLoc(World w)
    {
        return new Location(w, x, y, z, (float) yaw, (float) pitch);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
