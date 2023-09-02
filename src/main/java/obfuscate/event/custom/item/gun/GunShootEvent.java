package obfuscate.event.custom.item.gun;

import obfuscate.event.custom.player.PlayerUseItemEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Bullet;
import obfuscate.mechanic.item.guns.Gun;

public class GunShootEvent extends PlayerUseItemEvent {

    private final Bullet bullet;

    public Bullet getBullet() {
        return bullet;
    }

    public GunShootEvent(StrikePlayer player, Game Game, Gun gun, Bullet bullet) {
        super(player, Game, gun);
        this.bullet = bullet;
    }
}
