package obfuscate.util.alg;

import obfuscate.game.player.StrikePlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;


public class TraceEvent {
    private Player player;
    private Location rightBeforeHit;
    private Location rightAfterHit;

    public TraceEvent(Player player, Location rightBeforeHit, Location rightAfterHit) {
        this.player = player;
        this.rightBeforeHit = rightBeforeHit;
        this.rightAfterHit = rightAfterHit;
    }

    @Nullable
    public StrikePlayer getPlayer() {
        if (player == null) {
            return null;
        }
        return StrikePlayer.getOrCreate(player);
    }

    @Nullable
    public Location getRightBeforeHit() {
        if (rightBeforeHit == null)
            return null;

        return rightBeforeHit.clone();
    }

    @Nullable
    public Location getRightAfterHit() {
        if (rightAfterHit == null)
            return null;

        return rightAfterHit.clone();
    }

    public boolean didHit() {
        return rightBeforeHit != null;
    }

    public boolean didHitPlayer() {
        return player != null;
    }
}
