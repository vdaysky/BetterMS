package obfuscate.event.bukkit;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Bullet;
import obfuscate.mechanic.version.hitbox.HitArea;
import obfuscate.mechanic.version.PlayerLocation;
import org.bukkit.Location;

public class BulletHitPlayerEvent extends BulletHitEvent {

    private StrikePlayer player;
    private PlayerLocation damageeLocation;

    private HitArea area;

    public BulletHitPlayerEvent(Game game, Bullet bullet, Location hitLocation, Location preHitLocation, StrikePlayer player, PlayerLocation damageeLocation, HitArea area) {
        super(game, bullet, hitLocation, preHitLocation, player.getPlayer());
        this.player = player;
        this.damageeLocation = damageeLocation;
        this.area = area;
    }

    public StrikePlayer getHitPlayer() {
        return player;
    }

    public PlayerLocation getDamageeLocation() {
        return damageeLocation;
    }

    public HitArea getHitArea() {
        return area;
    }
}
