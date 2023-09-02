package obfuscate.event.custom.damage;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;
import obfuscate.game.damage.DamageModifiers;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.player.StrikePlayer;

public class StrikeDamageEvent extends CancellableEvent
{
    private final Game Game;
    private final StrikePlayer victim;
    private final StrikePlayer damager;

    private double damage;
    private NamedDamageSource damageSource;
    private DamageModifiers modifiers;

    public StrikeDamageEvent(Game Game, StrikePlayer victim, StrikePlayer damager, double damage, NamedDamageSource damageSource, DamageModifiers modifiers)
    {
        this.Game = Game;
        this.victim = victim;
        this.damager = damager;
        this.damage = damage;
        this.damageSource = damageSource;
        this.modifiers = modifiers;
    }
    public StrikeDamageEvent(Game Game, StrikePlayer victim, StrikePlayer damager, double damage, NamedDamageSource damageSource)
    {
        this.Game = Game;
        this.victim = victim;
        this.damager = damager;
        this.damage = damage;
        this.damageSource = damageSource;
        this.modifiers = new DamageModifiers();
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public Game getGame() {
        return Game;
    }

    public StrikePlayer getDamager() {
        return damager;
    }

    public StrikePlayer getDamagee() {
        return victim;
    }

    public double getDamage()
    {
        return damage;
    }

    public NamedDamageSource getDamageSource()
    {
        return damageSource;
    }

    public DamageModifiers getModifiers() {
        return modifiers;
    }
}
