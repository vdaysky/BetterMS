package obfuscate.event.custom.damage;

import obfuscate.event.custom.TrackedEvent;
import obfuscate.game.core.IGame;
import obfuscate.game.damage.DamageModifiers;
import obfuscate.game.damage.DamageReason;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.chat.Icon;
import obfuscate.util.serialize.dump.Serializable;
import obfuscate.util.chat.C;

public class PlayerDeathEvent extends TrackedEvent
{
    @Serializable
    private IGame game;
    @Serializable
    private StrikePlayer damagee;
    @Serializable
    private StrikePlayer damager;
    @Serializable(serializer = ":getName")
    private NamedDamageSource damageSource;

    @Serializable(serializer = "arbitrary")
    private DamageModifiers modifiers;

    @Serializable
    private DamageReason reason;

    @Serializable
    private Integer round;

    public PlayerDeathEvent(IGame game, StrikePlayer damagee, StrikePlayer damager, NamedDamageSource damageSource, DamageReason reason, DamageModifiers modifiers)
    {
        this.game = game;
        this.damagee = damagee;
        this.damageSource = damageSource;
        this.damager = damager;
        this.modifiers = modifiers;
        this.reason = reason;
        this.round = game.getRoundNumber();
    }

    public StrikePlayer getDamager() {
        return damager;
    }

    public IGame getGame() {
        return game;
    }

    public String getDescription(boolean useIcons) {
        if (useIcons) {
            return damageSource.getName() +
                    (modifiers.isNoscope() ? " " + Icon.NOSCOPE : "") +
                    (modifiers.isBlind() ? " " + Icon.BLIND + C.Reset : "") +
                    (modifiers.isWallbang() ? " " + Icon.WALLBANG : "") +
                    (modifiers.isThroughSmoke() ? " " + Icon.THROUGH_SMOKE : "") +
                    (modifiers.isHeadshot() ? " " + Icon.HEADSHOT : "");
        }
        return damageSource.getName() +
                (modifiers.isNoscope() ? ", (X)" : "") +
                (modifiers.isBlind() ? C.Strike + "<(*)>" + C.Reset : "") +
                (modifiers.isWallbang() ? ", -)->" : "") +
                (modifiers.isThroughSmoke() ? ", oOo-" : "") +
                (modifiers.isHeadshot() ? ", o`-" : "");
    }

    public StrikePlayer getDamagee() {
        return damagee;
    }

    public NamedDamageSource getDamageSource() {
        return damageSource;
    }

    public DamageModifiers getModifiers() {
        return modifiers;
    }

    public DamageReason getReason() {
        return reason;
    }
}
