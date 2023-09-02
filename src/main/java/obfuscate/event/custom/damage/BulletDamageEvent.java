package obfuscate.event.custom.damage;

import obfuscate.game.core.Game;
import obfuscate.game.damage.DamageModifiers;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Bullet;

public class BulletDamageEvent extends StrikeDamageEvent
{
    private final Bullet bullet;

    public BulletDamageEvent(
            Game Game,
            StrikePlayer victim,
            StrikePlayer damager,
            double damage,
            Bullet bullet,
            NamedDamageSource damageSource,
            DamageModifiers modifiers
    ) {
        super(Game, victim, damager, damage, damageSource, modifiers);
        this.bullet = bullet;
    }

    public Bullet getBullet() {
        return bullet;
    }
}
