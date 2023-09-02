package obfuscate.game.debug;

import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Bullet;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class BulletLog {

    private final Bullet bullet;
    private final Location shooterExactLocation;

    private final Long shootTime;

    private final ArrayList<BulletHit> history = new ArrayList<>();

    private final ArrayList<ClosePlayer> closePlayers = new ArrayList<>();

    public BulletLog(Bullet bullet, Location shooterExactLocation, Long shootTime) {
        this.bullet = bullet;
        this.shooterExactLocation = shooterExactLocation;
        this.shootTime = shootTime;
    }

    public void bulletHit(Location location, @Nullable StrikePlayer player, Long hitTime) {
        Location playerLocation = null;
        if (player != null) {
            playerLocation = player.getPlayer().getLocation();
        }
        history.add(new BulletHit(location, player, playerLocation, hitTime));
    }

    public void addClosePlayer(StrikePlayer player, Location loc, double distance) {
        this.closePlayers.add(new ClosePlayer(player, loc, distance));
    }

    public ArrayList<ClosePlayer> getClosePlayers() {
        return closePlayers;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Location getShooterExactLocation() {
        return shooterExactLocation;
    }

    public Long getShootTime() {
        return shootTime;
    }

    public ArrayList<BulletHit> getHistory() {
        return history;
    }
}
