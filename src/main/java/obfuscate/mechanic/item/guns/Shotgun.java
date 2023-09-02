package obfuscate.mechanic.item.guns;

import obfuscate.event.custom.item.gun.GunShootEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.util.recahrge.Recharge;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class Shotgun extends Gun {

    private final int _pellets;

    public Shotgun(GunStats gunStats)
    {
        super(gunStats);

        _pellets = gunStats.getPellets();
    }

    @Override
    public StrikeItem copy() {
        return new Shotgun((GunStats) getStats());
    }

    @Override
    public void shoot(StrikePlayer player, Game game)
    {
        if (!hasBullets())
        {
            reload(game, player, false);
            return;
        }

        if (isReloading())
            return;

        if (!Recharge.done(player, "Shoot", _gunStats.getFireRate(), () -> {
            if (_gunStats.getFireRate() >= 500)
            {
                if (!player.isOnline()) return;

                player.getLocation().getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
                player.getLocation().getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
            }
        })) {
            return;
        }

        //Effect
        soundFire(player.getLocation());
        player.getEyeLocation().getWorld().spawnParticle(Particle.CLOUD, player.getEyeLocation(), 0, 0, 0, 0, 1);

        for (int i=0 ; i<_pellets ; i++) {
            Bullet bullet = fireBullet(game, player);

            boolean cancelled = new GunShootEvent(player, game, this, bullet).triggerSync();

            if (cancelled) {
                bullet.remove();
                return;
            }
        }

        if (!game.getConfig().getValue(ConfigField.INFINITE_AMMO).eq(1))
        {
            bullets_left--;
            game.getGameSession(player).getHotbarMessenger().setMessage(printBulletsState());
        }

        //Reload
        if (!hasBullets())
            reload(game, player, false);
    }

    @Override
    public void soundRefire(Location loc)
    {
        if (_gunStats.getFireRate() >= 500)
        {
            loc.getWorld().playSound(loc, Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
            loc.getWorld().playSound(loc, Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
        }
    }

    @Override
    public long getReloadTime()
    {
        return _gunStats.getReloadTime() + _gunStats.getReloadTime() * Math.min(_spareAmmo, _gunStats.getClipSize() - bullets_left);
    }
}
