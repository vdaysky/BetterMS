package obfuscate.event.custom.damage;

import obfuscate.game.core.Game;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.player.StrikePlayer;

public class MeleeDamageEvent extends StrikeDamageEvent
{
    private boolean backStab;

    public boolean isBackStab() {
        return backStab;
    }

    public MeleeDamageEvent(Game Game, StrikePlayer damagee, StrikePlayer damager, double damage, boolean backStab, NamedDamageSource damageSource) {
        super(Game, damagee, damager, damage, damageSource);
        this.backStab = backStab;
    }

}
