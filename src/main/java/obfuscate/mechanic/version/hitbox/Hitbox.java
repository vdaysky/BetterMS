package obfuscate.mechanic.version.hitbox;

import obfuscate.mechanic.version.PlayerLocation;
import org.bukkit.Location;

public interface Hitbox
{
    boolean hitHead(PlayerLocation player, Location loc);
    boolean hitBody(PlayerLocation player, Location loc);

    default HitArea hits(Location start, PlayerLocation player) {
        if (hitBody(player, start)) {
            return HitArea.BODY;
        }
        if (hitHead(player, start)) {
            return HitArea.HEAD;
        }

        return HitArea.MISS;
    }
}
