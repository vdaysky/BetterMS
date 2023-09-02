package obfuscate.mechanic.item.utility.grenade.engine;

import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.UtilAction;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class OgGrenadePhysics implements GrenadePhysics {

    private Entity ent;

    private Vector _vel;
    private Location _lastLoc;
    private ArrayList<Vector> _velHistory = new ArrayList<>();

    private boolean touchedGround = false;
    private int groundTicks = 0;

    @Override
    public Location getCurrentLocation() {
        return ent.getLocation();
    }

    @Override
    public void launch(ItemStack stack, StrikePlayer player, boolean wasLeftClick, Game game) {
        ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()), stack);

        float longThrowVelocity = game.get(ConfigField.GRENADE_OG_THROW_SPEED) / 100f;
        float shortThrowSpeed = game.get(ConfigField.GRENADE_OG_TOSS_SPEED) / 100f;

        if (wasLeftClick)
            UtilAction.velocity(ent, player.getLocation().getDirection(), shortThrowSpeed, false, 0, 0, 2, false);
        else
            UtilAction.velocity(ent, player.getLocation().getDirection(), longThrowVelocity, false, 0, 0.2, 2, false);

        //Rebound Data
        _vel = player.getLocation().getDirection();
        _velHistory.add(_vel);
        _lastLoc = ent.getLocation();

    }

    @Override
    public void removeEntity() {
        ent.remove();
    }

    @Override
    public boolean update(Game game) {

        if (ent.isOnGround()) {
            touchedGround = true;
            groundTicks += 1;
        }

        //Invalid
        return !ent.isValid();
    }

    @Override
    public boolean touchedGround() {
        return touchedGround;
    }

    @Override
    public void multiplyVelocity(float f) {

    }

    @Override
    public void moveRebounding(Game game) {
        if (ent.isOnGround() || ent.getVelocity().length() < 0.1 || ent.getTicksLived() < 4)
            return;

        /*
         * What we must do here, is record the velocity every tick.
         * Then when it collides, we get the velocity from a few ticks before and apply it, reversing the direction of collision.
         * We record history because as soon as it collides the collision direction is set to 0.
         */

        //X Rebound
        if ((_vel.getX() > 0.05 && ent.getLocation().getX() - _lastLoc.getX() <= 0) ||
                (_vel.getX() < -0.05 && ent.getLocation().getX() - _lastLoc.getX() >= 0))
        {
            _vel = _velHistory.get(0);
            _vel.setX(-_vel.getX());
            _vel.multiply(0.75);

            ent.setVelocity(_vel);

            ent.getWorld().playSound(ent.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1f, 2f);
        }

        //Z Rebound
        else if ((_vel.getZ() > 0.05 && ent.getLocation().getZ() - _lastLoc.getZ() <= 0) ||
                (_vel.getZ() < -0.05 && ent.getLocation().getZ() - _lastLoc.getZ() >= 0))
        {
            _vel = _velHistory.get(0);
            _vel.setZ(-_vel.getZ());
            _vel.multiply(0.75);

            ent.setVelocity(_vel);

            ent.getWorld().playSound(ent.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1f, 2f);
        }

        else
        {
            _velHistory.add(ent.getVelocity());

            while (_velHistory.size() > 4)
                _velHistory.remove(0);
        }

        _lastLoc = ent.getLocation();
    }

    @Override
    public int getGroundTicks() {
        return groundTicks;
    }
}
