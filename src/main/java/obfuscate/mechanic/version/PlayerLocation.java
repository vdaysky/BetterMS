package obfuscate.mechanic.version;

import org.bukkit.Location;

public class PlayerLocation {

    private final Location location;
    private final Location eyeLocation;

    public PlayerLocation(Location location, Location eyeLocation) {
        this.location = location.clone();
        this.eyeLocation = eyeLocation.clone();
    }

    public Location getLocation() {
        return location.clone();
    }

    public Location getEyeLocation() {
        return eyeLocation.clone();
    }
}
