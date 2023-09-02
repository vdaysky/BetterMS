package obfuscate.util.alg;

import obfuscate.mechanic.version.hitbox.HitArea;
import obfuscate.mechanic.version.PlayerLocation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TraceEventV2 {

    private Location entrypoint;

    private Location preHit;

    private boolean isEntry;

    private Entity entity;

    private PlayerLocation loc;

    private HitArea area;

    private boolean isSmoke;

    public TraceEventV2(Location entrypoint, Location preEntry, boolean isEntry, Entity entity, PlayerLocation loc, HitArea area, boolean isSmoke) {
        this.entrypoint = entrypoint;
        this.isEntry = isEntry;
        this.preHit = preEntry;
        this.entity = entity;
        this.loc = loc;
        this.area = area;
        this.isSmoke = isSmoke;
    }

    public boolean isSmoke() {
        return isSmoke;
    }

    public Location getLocation() {
        return entrypoint.clone();
    }

    public Location getPreLocation() {
        return preHit.clone();
    }

    public boolean isEntry() {
        return isEntry;
    }

    public boolean isExit() {
        return !isEntry;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean blockEvent() {
        return entity == null;
    }

    public boolean hitEntity() {
        return entity != null;
    }

    public PlayerLocation getPlayerLocation() {
        return loc;
    }

    public HitArea getHitArea() {
        return area;
    }
}
