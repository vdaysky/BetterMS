package obfuscate.game.debug;

import obfuscate.game.player.StrikePlayer;
import org.bukkit.Location;

public class BulletHit {
    private final Location hitLocation;
    private final StrikePlayer hitPlayer;

    private final Location exactPlayerLocation;

    private final Long hitTime;

    public BulletHit(Location hitLocation, StrikePlayer hitPlayer, Location exactPlayerLocation, Long hitTime) {
        this.hitLocation = hitLocation;
        this.hitPlayer = hitPlayer;
        this.exactPlayerLocation = exactPlayerLocation;
        this.hitTime = hitTime;
    }

    public Location getHitLocation() {
        return hitLocation;
    }

    public StrikePlayer getHitPlayer() {
        return hitPlayer;
    }

    public Location getExactPlayerLocation() {
        return exactPlayerLocation;
    }

    public Long getHitTime() {
        return hitTime;
    }
}
