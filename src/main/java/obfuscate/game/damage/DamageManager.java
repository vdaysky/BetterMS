package obfuscate.game.damage;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.damage.*;
import obfuscate.game.core.Game;
import obfuscate.game.core.GameInventory;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.TeamGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.StateTag;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.guns.Bullet;
import obfuscate.mechanic.version.hitbox.HitArea;
import obfuscate.mechanic.version.PlayerLocation;
import obfuscate.util.alg.UtilAlg;
import obfuscate.util.UtilPlayer;
import obfuscate.util.recahrge.Recharge;
import org.bukkit.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.HashMap;

/** what DamageManager is just handles all kind of damage in the way it is intended in some gamemode.
 *  messages and effects are handled by game. */
public class DamageManager
{
    private final Game _game;
    public DamageManager(Game game)
    {
        _game = game;
    }

    public Game getGame()
    {
        return _game;
    }

    public void handleMeleeAttack(Game game, StrikePlayer damagee, StrikePlayer damager, NamedDamageSource damageSource)
    {
        if (damagee.immuneToKnife(game) )
        {
            damager.sendTitle("", "Stop it!", 0, 40, 0);
            return;
        }

        Vector look = damagee.getLocation().getDirection();
        look.setY(0);
        look.normalize();

        Vector from = damager.getLocation().toVector().subtract(damagee.getLocation().toVector());
        from.setY(0);
        from.normalize();

        Vector check = new Vector(look.getX() * -1, 0, look.getZ() * -1);

        boolean backstab = check.subtract(from).length() < 0.8;

        DamageModifiers modifiers = new DamageModifiers();
        modifiers.setBackstab(backstab);

        double damage = calculateAndApplyActualDamage(game, damagee, damager, 6, damageSource, DamageReason.KNIFE, modifiers);

        new MeleeDamageEvent(_game, damagee, damager, damage, backstab, damageSource).trigger();
    }

    public void handleGunDamage(Game game, StrikePlayer damagee, StrikePlayer damager, PlayerLocation damageeLocation, Bullet bullet, Gun damageSource, HitArea area)
    {
        DamageModifiers modifiers = new DamageModifiers();
        
        if (bullet == null) {
            return;
        }

        // damager is dead, prevent damage
        if (!game.getGameSession(damager).isAlive()) {
            return;
        }

        // prevent bullet flying from shoulder hitting shooter
        if (damagee == damager) {
            return;
        }

        if (area == HitArea.MISS) {
            MsdmPlugin.highlight(">  Bullet missed");
            return;
        }

        // head
        if (area == HitArea.HEAD)
        {
            modifiers.setHeadshot(true);
        }

        modifiers.setNoscope(bullet.wasNoScope());

        if (damager != null && damager.hasEffect(PotionEffectType.BLINDNESS)) {
            modifiers.setBlind(true);
        }

        if (bullet.wasInSmoke()) {
            modifiers.setThroughSmoke(true);
        }

        double damage = bullet.getDamage();

        // Decrease damage // todo why armor penetration is not modifier and rest is?
        GameInventory inventory = game.getGameSession(damagee).getInventory();
        if ( ( area == HitArea.BODY && inventory.hasKevlar() ) || ( area == HitArea.HEAD && inventory.hasHelmet() ) ) {
            damage *= bullet.getGun().getArmorPenetration();
        }

        // add wallbang modifier
        modifiers.setWallbangPenalty(bullet.getWallbangPenalty());

        double passedDamage = calculateAndApplyActualDamage(game, damagee, damager, damage, damageSource, DamageReason.GUN, modifiers);
        if (passedDamage < 1f) { // todo: make sure this didn't break anything
            return;
        }

        // only if damage passes
        if (area == HitArea.HEAD) {
            new HeadshotEvent(game, damagee, damager).trigger();
        }

        // Trigger Event
        new BulletDamageEvent(
                game,
                damagee,
                damager,
                damage,
                bullet,
                damageSource,
                modifiers
        ).trigger();
    }

    public void handleFallDamage(Game game, StrikePlayer damagee, double originalDamage)
    {
        var damageSource = new DamageSourceWrapper("Fall");
        double damage = Math.max(0, originalDamage - 2);

        calculateAndApplyActualDamage(game, damagee, null, damage, damageSource, DamageReason.FALL);
        new FallDamageEvent(game, damagee, null, damage, damageSource).trigger();
    }

    public void handleBurnDamage(Game game, StrikePlayer damagee, @Nullable StrikePlayer damager, NamedDamageSource damageSource)
    {
        // player can take burn damage 10 times a second
        if (!Recharge.done(damagee, "burn", 100)) {
            return;
        }

        double damage = 1;
        calculateAndApplyActualDamage(game, damagee, damager, damage, damageSource, DamageReason.FIRE);
        new PlayerBurnEvent(game, damagee, damager, damage, damageSource).trigger();
    }

    private double calculateAndApplyActualDamage(Game game, StrikePlayer damagee, StrikePlayer damager, double baseDamage, NamedDamageSource damageSource, DamageReason reason){
        return calculateAndApplyActualDamage(game, damagee, damager, baseDamage, damageSource, reason, new DamageModifiers());
    }

    private double calculateAndApplyActualDamage(
            Game game,
            StrikePlayer damagee,
            StrikePlayer damager,
            double baseDamage,
            NamedDamageSource damageSource,
            DamageReason reason,
            DamageModifiers modifiers
    )
    {
        double calculatedDamage = modifiers.apply(baseDamage);

        if (calculatedDamage < 0) {
            return 0;
        }

        // check if damage allowed
        if (!canTakeDamage(game, calculatedDamage, damagee, damager)) {
            return 0;
        }

        // display dealt damage
        if (damager != null) {
            damager.getPlayer().setLevel((int) calculatedDamage);
        }

        game.getDamageLog().logDealtDamage(damager, damagee, damageSource, modifiers, calculatedDamage);

        if (!damagee.canSurviveDamage(calculatedDamage)) {
            new PlayerDeathEvent(_game, damagee, damager, damageSource, reason, modifiers).trigger();
        }
        else {
            damagee.damage(calculatedDamage, game);
        }

        return calculatedDamage;
    }

    private boolean canTakeDamage(Game game, double damage, StrikePlayer damagee, StrikePlayer damager)
    {
        if (!damagee.isAlive(game)) {
            return false;
        }

        if (damage == 0) {
            return false;
        }


        if ( damagee.isInvulnerable(game) ) {
            return false;
        }

        if (getGame() instanceof TeamGame)
        {
            TeamGame teamGame = (TeamGame) game;
            if (damagee != damager && teamGame.areInSameTeam(damagee, damager) && !game.getConfig().getValue(ConfigField.ALLOW_TEAM_DAMAGE).bool()) {
                return false;
            }

        }

        // damage allowed by state and by general gamerule
        if (game.getGameState().is(StateTag.DAMAGE_ALLOWED) && game.getConfig().getValue(ConfigField.ALLOW_DAMAGE).bool()) {
            return true;
        }
        return false;
    }

    /** handles explosion damage
     *
     * @param location center of explosion
     * @param maxDamage maximal damage explosion can make
     * @param range maximal range where explosion does damage
     * @param wallPenetration how much damage makes through 1-block wall 1 - full damage, 0 no damage
     * @param damageSource damage source
     * */
    public void handleExplosion(
            Game game,
            StrikePlayer damager,
            Location location,
            int maxDamage,
            int range,
            double wallPenetration,
            StrikeItem damageSource,
            DamageReason reason
    )
    {
        HashMap<StrikePlayer, Double> players = UtilPlayer.getInRadius(location, range);

        for (StrikePlayer player : players.keySet())
        {
            if (!player.isAlive(game))
                continue;

            double damage = 1 + (players.get(player) * maxDamage);
            int walls = UtilAlg.countWalls(location, player.getEyeLocation());
            damage *= Math.pow(wallPenetration, walls);

            if (player.getInventory(game).hasKevlar()) {
                damage *= 0.7;
            }

            double actualDamage = calculateAndApplyActualDamage(game, player, damager, damage, damageSource, reason);
            new ExplosionDamageEvent(game, player, damager, actualDamage, damageSource).trigger();
        }
    }
}
