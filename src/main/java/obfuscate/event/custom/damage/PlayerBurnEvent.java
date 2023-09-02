package obfuscate.event.custom.damage;

import obfuscate.game.core.Game;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.player.StrikePlayer;

public class PlayerBurnEvent extends StrikeDamageEvent
{
    public PlayerBurnEvent(Game Game, StrikePlayer victim, StrikePlayer damager, double damage, NamedDamageSource damageSource) {
        super(Game, victim, damager, damage, damageSource);
    }
}
