package obfuscate.game.debug;

import obfuscate.game.player.StrikePlayer;
import org.bukkit.Location;

public class ClosePlayer {

    private final StrikePlayer player;

    private final Location loc;

    private final double distance;

    public ClosePlayer(StrikePlayer player, Location loc, double distance) {
        this.player = player;
        this.loc = loc;
        this.distance = distance;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public Location getLoc() {
        return loc;
    }

    public Double getDistance() {
        return distance;
    }
}
