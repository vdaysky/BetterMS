package obfuscate.event.bukkit;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Bullet;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;


public class BulletHitEvent extends CustomEvent {

    private Bullet bullet;
    private Location hitLocation;
    private Location preHitLocation;
    private Entity entity;
    private Game game;

    public BulletHitEvent(Game game, Bullet bullet, Location hitLocation, Location preHitLocation, @Nullable Entity entity) {
        this.bullet = bullet;
        this.hitLocation = hitLocation;
        this.preHitLocation = preHitLocation;
        this.entity = entity;
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Location getHitLocation() {
        return hitLocation;
    }

    public Location getPreLocation() {
        return preHitLocation;
    }

    public Entity getHitEntity() {
        return entity;
    }

    public StrikePlayer getHitPlayer() {
        if (getHitEntity() instanceof Player player) {
            return StrikePlayer.getOrCreate(player);
        }
        return null;
    }
}
