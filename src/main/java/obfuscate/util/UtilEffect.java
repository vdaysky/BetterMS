package obfuscate.util;

import obfuscate.game.debug.BulletHit;
import obfuscate.game.debug.BulletLog;
import obfuscate.game.debug.ClosePlayer;
import obfuscate.game.player.BotPlayer;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.time.Task;
import de.slikey.effectlib.util.ParticleEffect;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.HashSet;


public class UtilEffect {

    /* Create effect of chicken exploding with feather items */
    public static void chickenExplosion(Location loc) {

        for (int i = 0; i < 10; i++) {
            ItemStack feather = new ItemStack(Material.FEATHER);
            var meta = feather.getItemMeta();
            meta.setDisplayName("" + i + loc.hashCode());
            feather.setItemMeta(meta);
            Item e = loc.getWorld().dropItem(loc, feather);
            e.setPickupDelay(100000);
            e.setVelocity(new Vector(Math.random() - 0.5, Math.random() * 0.5, Math.random() - 0.5));
            UtilParticle.PlayParticle(ParticleEffect.CLOUD, loc, 0, 0, 0, 0, 2);
            new Task(e::remove, (int)(Math.random() * 5 * 20)).run();
        }
    }

    public static void Effects(StrikePlayer player, Location loc, int particles, double velMult, Sound sound,
                        float soundVol, float soundPitch, Material type, byte data, boolean bloodStep)
    {
        Effects(player, loc, particles, velMult, sound, soundVol, soundPitch, type, data, 10, bloodStep);
    }

    public static void Effects(StrikePlayer player, Location loc, int particles, double velMult, Sound sound,
                        float soundVol, float soundPitch, Material type, byte data, int ticks, boolean bloodStep)
    {
        for (int i = 0 ; i < particles ; i++)
        {
            var stack = new ItemStack(type);
            var meta = stack.getItemMeta();
            meta.setDisplayName("" + System.nanoTime());
            stack.setItemMeta(meta);
            stack.setAmount(1);

            Item item = loc.getWorld().dropItem(loc, stack);

            item.setVelocity(new Vector((Math.random() - 0.5)*velMult,Math.random()*velMult,(Math.random() - 0.5)*velMult));

            item.setPickupDelay(999999);

            new Task(item::remove, ticks).run();
        }

        if (bloodStep) {
            loc.getWorld().playEffect(loc, Effect.STEP_SOUND, 55);
        }


        loc.getWorld().playSound(loc, sound, soundVol, soundPitch);
    }

    public static void playerDeath(StrikePlayer player) {

        Effects(player, player.getEyeLocation(), 10, 0.5, Sound.ENTITY_PLAYER_DEATH,
                1f, 1f, Material.RED_DYE, (byte)1, true);
        return;
//        Location eyeLoc = player.getEyeLocation();
//
//        for (int i = 0; i < 10; i++) {
//            ItemStack bloodItem = new ItemStack(Material.RED_DYE);
//
//            var meta = bloodItem.getItemMeta();
//            meta.setDisplayName("" + i + eyeLoc.hashCode());
//            bloodItem.setItemMeta(meta);
//
//            // drop in circle around player
//            double dx = Math.cos(i * Math.PI / 5);
//            double dz = Math.sin(i * Math.PI / 5);
//
//            Location dropLocUp = eyeLoc.clone().add(dx, 0, dz);
//            Location dropLocDown = eyeLoc.clone().add(dx, -0.5, dz);
//            Item entUp = player.getWorld().dropItem(dropLocUp, bloodItem);
//            Item entDown = player.getWorld().dropItem(dropLocDown, bloodItem);
//            entUp.setPickupDelay(9999);
//            entUp.setVelocity(new Vector(dx, 1.5, dz).multiply(0.2));
//
//            entDown.setPickupDelay(9999);
//            entDown.setVelocity(new Vector(dx, 1.5, dz).multiply(0.2));
//
//            new Task(
//                    entDown::remove,
//                    (int)(Math.random() * 1 * 20)
//            ).run();
//            new Task(
//                    entUp::remove,
//                    (int)(Math.random() * 1 * 20)
//            ).run();
//        }
    }

    public static void replayShot(
            BulletLog log,
            int playForTicks
    ) {

        // trace from shooter to victim
        Location start = log.getBullet().getOrigin().clone();

        // display shooter
        // TODO: this bot is still not part of the game, which might cause problems.
        //       a possible solution would be to ignore players that are not in game,
        //       or set explicit flag to ignore some of the players
        BotPlayer shooterNPC = BotPlayer.getOrCreate(log.getBullet().getShooter().getName(), log.getShooterExactLocation());
        shooterNPC.getNPC().setFlyable(true);
        shooterNPC.getNPC().setProtected(true);
        shooterNPC.getNPC().getEntity().setGravity(false);

        HashSet<StrikePlayer> hitPlayers = new HashSet<>();

        for (BulletHit hit : log.getHistory()) {
            final Location to = hit.getHitLocation();
            Vector direction = to.clone().subtract(start).toVector().normalize().multiply(0.5);

            int delay = 5;
            while (start.distance(to) > 0.5) {
                start.add(direction);
                for (int tick = 0; tick < playForTicks / delay; tick++) {
                    Location loc = start.clone();
                    new Task(
                            () -> UtilParticle.PlayParticle(ParticleEffect.REDSTONE, loc, 0, 0, 0, 0, 1),
                            tick * delay
                    ).run();
                }
            }

            BotPlayer victimNPC = null;

            if (hit.getHitPlayer() != null) {
                hitPlayers.add(hit.getHitPlayer());

                victimNPC = BotPlayer.getOrCreate(hit.getHitPlayer().getName(), hit.getExactPlayerLocation());
                victimNPC.getNPC().setFlyable(true);
                victimNPC.getNPC().setProtected(true);
                victimNPC.getNPC().getEntity().setGravity(false);
            }

            final BotPlayer _victim = victimNPC;

            new Task(() -> {
                shooterNPC.getNPC().destroy();
                if (_victim != null) _victim.getNPC().destroy();
            }, playForTicks).run();
        }

        // display close hits
        for (ClosePlayer closePlayer : log.getClosePlayers()) {

            // well, too close
            if (hitPlayers.contains(closePlayer.getPlayer())) continue;

            if (closePlayer.getPlayer().equals(log.getBullet().getShooter())) continue;

            var df = new DecimalFormat("0.00");
            String displayName = "Missed (" + df.format(closePlayer.getDistance()) + "m)";
            final NPC closePlayerNPC = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, displayName);

            closePlayerNPC.setFlyable(true);
            closePlayerNPC.setProtected(true);
            closePlayerNPC.spawn(closePlayer.getLoc());
            closePlayerNPC.getEntity().setGravity(false);
            SkinTrait skinTrait = closePlayerNPC.getTrait(SkinTrait.class);
            skinTrait.setSkinName(closePlayer.getPlayer().getName());

            // delete npc after
            new Task(closePlayerNPC::destroy,playForTicks).run();
        }
    }


    public static void showLine(Location start, Location end, ParticleEffect effect, double step) {
        Vector direction = end.clone().subtract(start).toVector().normalize().multiply(step);
        int sanityCheck = 0;

        while (start.distance(end) > 0.5 && sanityCheck++ < 1000) {
            start.add(direction);
            UtilParticle.PlayParticle(effect, start, 0, 0, 0, 0, 1);
        }
    }
    public static void showLine(Location start, Location end, ParticleEffect effect) {
        showLine(start, end, effect, 0.5);
    }

    public static void displayBoundingBox(World w, BoundingBox bb, ParticleEffect effect) {
        double minX = bb.getMin().getX();
        double minY = bb.getMin().getY();
        double minZ = bb.getMin().getZ();

        double maxX = bb.getMax().getX();
        double maxY = bb.getMax().getY();
        double maxZ = bb.getMax().getZ();

        // bottom
        showLine(new Location(w, minX, minY, minZ), new Location(w, maxX, minY, minZ), effect);
        showLine(new Location(w, minX, minY, minZ), new Location(w, minX, minY, maxZ), effect);
        showLine(new Location(w, maxX, minY, minZ), new Location(w, maxX, minY, maxZ), effect);
        showLine(new Location(w, minX, minY, maxZ), new Location(w, maxX, minY, maxZ), effect);

        // top
        showLine(new Location(w, minX, maxY, minZ), new Location(w, maxX, maxY, minZ), effect);
        showLine(new Location(w, minX, maxY, minZ), new Location(w, minX, maxY, maxZ), effect);
        showLine(new Location(w, maxX, maxY, minZ), new Location(w, maxX, maxY, maxZ), effect);
        showLine(new Location(w, minX, maxY, maxZ), new Location(w, maxX, maxY, maxZ), effect);

        // sides
        showLine(new Location(w, minX, minY, minZ), new Location(w, minX, maxY, minZ), effect);
        showLine(new Location(w, minX, minY, maxZ), new Location(w, minX, maxY, maxZ), effect);
        showLine(new Location(w, maxX, minY, minZ), new Location(w, maxX, maxY, minZ), effect);
        showLine(new Location(w, maxX, minY, maxZ), new Location(w, maxX, maxY, maxZ), effect);
    }
}
