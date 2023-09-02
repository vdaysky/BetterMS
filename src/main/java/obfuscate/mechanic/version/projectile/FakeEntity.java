package obfuscate.mechanic.version.projectile;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.EntitySnowball;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class FakeEntity {

    private final Entity ent;
    private final Location currentLocation;
    private final World world;
    private ArrayList<Player> spawned_for = new ArrayList<>();

    public FakeEntity(Class<?extends Entity> cls, Location loc) {
        world = loc.getWorld();
        WorldServer s = ((CraftWorld) world).getHandle();

        ent = new EntitySnowball(s, 0,0,0);
        ent.setLocation(loc.getX(), loc.getY(), loc.getZ(), 0, 0);
        ent.setNoGravity(true);
        ent.setMot(0, 0, 0);

        // I don't clone it so location of fake entity is linked to whatever created it
        currentLocation = loc;
    }

    public void spawn() {
        for (Player player : world.getPlayers()) {
            spawnFor(player);
        }
    }

    public void setLocation(Location loc) {
        this.currentLocation.setX(loc.getX());
        this.currentLocation.setY(loc.getY());
        this.currentLocation.setZ(loc.getZ());
    }

    public void spawnFor(Player player) {

        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(ent);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, PacketContainer.fromPacket(packet1));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        spawned_for.add(player);
    }

    /** smoothly animate projectile move from current location to given point
     * and then update actual location value
     *
     * @param end end of trajectory */
    public void teleport(Location end) {
        animate(end);
    }

    private void animate(Location next) {
        ent.setLocation(next.getX(), next.getY(), next.getZ(), next.getYaw(), next.getPitch());
        PacketPlayOutEntityTeleport p = new PacketPlayOutEntityTeleport(ent);

        for (Player player : next.getWorld().getPlayers()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, PacketContainer.fromPacket(p));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public int getEntityId() {
        return ent.getId();
    }

    // very bad
    private int ticks = 0;
    public int getTicksLived() {
        return ++ticks;
    }

    public void remove() {
        PacketPlayOutEntityDestroy p = new PacketPlayOutEntityDestroy(getEntityId());

        for (Player player : world.getPlayers()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, PacketContainer.fromPacket(p));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void respawn() {
        spawn();
    }

}
