package obfuscate.game.player;

import org.bukkit.Location;

public class PlayerMovement {

    private final double dx;
    private final double dy;
    private final double dz;
    private final long timestamp;

    private final double dYaw;
    private final double dPitch;

    public PlayerMovement(double dx, double dy, double dz, double dYaw, double dPitch, long timestamp) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.dYaw = dYaw;
        this.dPitch = dPitch;
        this.timestamp = timestamp;
    }

    public PlayerMovement(Location from, Location to, long timestamp) {
        this(
            to.getX() - from.getX(),
            to.getY() - from.getY(),
            to.getZ() - from.getZ(),
            to.getYaw() - from.getYaw(),
            to.getPitch() - from.getPitch(),
            timestamp
        );
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public double getDz() {
        return dz;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getYawChange() {
        return dYaw;
    }

    public double getPitchChange() {
        return dPitch;
    }
}
