package obfuscate.world;

import obfuscate.game.player.StrikePlayer;
import obfuscate.team.StrikeTeam;
import obfuscate.util.UtilEffect;
import obfuscate.util.Position;
import obfuscate.util.UtilItem;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.HashMap;

public class WorldTools {

    private static final HashMap<StrikePlayer, BoundingBox> selections = new HashMap<>();

    public static final HashMap<String, HashMap<Entity, Position>> positionIndicator = new HashMap<>();

    private static final HashMap<StrikePlayer, Location> firstPoint = new HashMap<>();

    public static void selectFirstPoint(StrikePlayer player, Location point) {
        firstPoint.put(player, point);
    }

    public static boolean hasFirstPoint(StrikePlayer player) {
        return firstPoint.containsKey(player);
    }

    public static boolean selectSecondPoint(StrikePlayer player, Location point) {
        Location first = firstPoint.get(player);
        if (first == null) {
            return false;
        }
        BoundingBox box = BoundingBox.of(first, point);
        box.expand(0, 0, 0, 1, 1, 1);
        selections.put(player, box);
        return true;
    }

    public static void resize(StrikePlayer player, int x1, int y1, int z1, int x2, int y2, int z2) {
        BoundingBox bb = selections.get(player);
        if (bb == null) {
            return;
        }
        bb.resize(x1, y1, z1, x2, y2, z2);
    }

    public static BoundingBox getSelection(StrikePlayer player) {
        return selections.get(player);
    }

    public static void setSelection(StrikePlayer player, BoundingBox bb) {
        selections.put(player, bb);
    }

    public static void unselect(StrikePlayer player) {
        selections.remove(player);
        firstPoint.remove(player);
    }

    /** TODO: this entire selector thing is a mess! it registers ton of stuff but never deletes it!
     *      Should only be used once to configure maps, and then forgotten about until new map update.
     *      or fixed
     * */
    public static void showBoundingBoxes(TempMap map) {
        map.getMapData().getASite().getBoxes().forEach(box -> {
            UtilEffect.displayBoundingBox(map.getWorld(), box, ParticleEffect.CRIT);
        });
        map.getMapData().getBSite().getBoxes().forEach(box -> {
            UtilEffect.displayBoundingBox(map.getWorld(), box, ParticleEffect.CRIT);
        });
        map.getMapData().getShop(StrikeTeam.T).getBoxes().forEach(box -> {
            UtilEffect.displayBoundingBox(map.getWorld(), box, ParticleEffect.CRIT);
        });
        map.getMapData().getShop(StrikeTeam.CT).getBoxes().forEach(box -> {
            UtilEffect.displayBoundingBox(map.getWorld(), box, ParticleEffect.CRIT);
        });

        // player selections
        for (StrikePlayer player : selections.keySet()) {
            UtilEffect.displayBoundingBox(map.getWorld(), selections.get(player), ParticleEffect.REDSTONE);
        }
    }

    public static void showRespawns(TempMap map) {
        for (Position pos : map.getMapData().getTeamRespawns(StrikeTeam.CT)) {
            showLocation(map.getWorld(), pos, StrikeTeam.CT);
        }
        for (Position pos : map.getMapData().getTeamRespawns(StrikeTeam.T)) {
            showLocation(map.getWorld(), pos, StrikeTeam.T);
        }
        for (Position pos : map.getMapData().getDMRespawns()) {
            showLocation(map.getWorld(), pos, null);
        }
    }
    public static void hideRespawns(TempMap map) {

        positionIndicator.remove(map.getWorld().getName());

        for (Position pos : map.getMapData().getTeamRespawns(StrikeTeam.CT)) {
            hideLocation(map.getWorld(), pos);
        }
        for (Position pos : map.getMapData().getTeamRespawns(StrikeTeam.T)) {
            hideLocation(map.getWorld(), pos);
        }
        for (Position pos : map.getMapData().getDMRespawns()) {
            hideLocation(map.getWorld(), pos);
        }
    }

    public static void showLocation(World world, Position pos, StrikeTeam team) {
        var loc = pos.toLoc(world);
        ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
        positionIndicator.computeIfAbsent(world.getName(), k -> new HashMap<>()).put(stand, pos);
        
        stand.setVisible(true);
        stand.setGravity(false);

        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        if (team != null) {
            UtilItem.colorArmor(chest, team.getArmorColor());
            stand.setChestplate(chest);
        }
    }

    public static void hideLocation(World world, Position pos) {
        var loc = pos.toLoc(world);
        world.getNearbyEntities(loc, 1, 1, 1).forEach(entity -> {
            if (entity instanceof ArmorStand) {
                entity.remove();
            }
        });
    }

    public static String getLookDirection(StrikePlayer player) {
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();

        if (pitch >= 45) {
            return "down";
        }
        if (pitch <= -45) {
            return "up";
        }
        if (yaw >= -45 && yaw <= 45) {
            return "south";
        }
        if (yaw >= 45 && yaw <= 135) {
            return "west";
        }
        if (yaw >= 135 && yaw <= 180 || yaw >= -180 && yaw <= -135) {
            return "north";
        }
        if (yaw >= -135 && yaw <= -45) {
            return "east";
        }
        throw new RuntimeException("My bad, can't get direction for " + pitch + " " + yaw);
    }
}
