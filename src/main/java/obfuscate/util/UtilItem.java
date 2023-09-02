package obfuscate.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;

public class UtilItem
{
    public static ItemStack makeUnbreakable(ItemStack item){
        net.minecraft.world.item.ItemStack is = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = is.hasTag() ? is.getTag() : new NBTTagCompound();
        tag.setInt("Unbreakable", 1);
        is.setTag(tag);
        return CraftItemStack.asBukkitCopy(is);
    }

    public static void colorArmor(ItemStack i, Color c) {
        LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
        meta.setColor(c);
        i.setItemMeta(meta);
    }

    public static void setInvulnerable(Entity entity) {

        try {
            Item item = (Item) entity;
            //net.minecraft.server.v1_8_R3.Entity handle = ((CraftItem) item).getHandle();
            //Bukkit.broadcastMessage("nbt: " + handle.getNBTTag());
//            NBTTagCompound nbt = new NBTTagCompound();
//            handle.c(nbt);
//            nbt.setBoolean("Invulnerable", true);
//            nbt.setInt("Invulnerable", 1);
//            handle.f(nbt);


            try {
                Object nmsEntity = entity.getClass().getMethod("getHandle").invoke(entity);

                Method method = nmsEntity.getClass().getMethod("getNBTTag");
                Object tag = method.invoke(nmsEntity);
                if(tag == null) {
                    tag = new NBTTagCompound();
                }
                method = nmsEntity.getClass().getMethod("c", NBTTagCompound.class);
                method.invoke(nmsEntity, tag);

                //Вот тут прописываем NBT Теги:
                //Допустим ставим Age:-32768
                tag.getClass().getMethod("setInt", String.class, int.class).invoke(tag, "Age", -32768);
                tag.getClass().getMethod("setInt", String.class, int.class).invoke(tag, "Invulnerable", 1);
                tag.getClass().getMethod("setBoolean", String.class, boolean.class).invoke(tag, "Invulnerable", true);
                nmsEntity.getClass().getMethod("a", NBTTagCompound.class).invoke(nmsEntity, tag);


            } catch (Exception x) {
                x.printStackTrace();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void spawnClientSide(Item item, Player p)
    {
        spawnClientSide(item.getLocation(), item.getItemStack().clone(), item.getVelocity(), p);
    }

    public static void spawnClientSide(Location loc, ItemStack stack, Vector velocity, Player p)
    {

        Bukkit.broadcastMessage("spawned " + stack.getType() + " at " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " for "+ p.getName() );
        EntityItem item = new EntityItem(
                ((CraftWorld) loc.getWorld()).getHandle(),
                loc.getX(), loc.getY(), loc.getZ(),
                CraftItemStack.asNMSCopy(stack)
        );
        NBTTagCompound tag = new NBTTagCompound();
        item.loadData(tag);
        tag.setInt( "Invulnerable", 1);
        tag.setBoolean("Invulnerable", true);
        item.saveData(tag);

        PacketPlayOutSpawnEntity spawnItem = new PacketPlayOutSpawnEntity(item);
        PacketPlayOutEntityMetadata data = new PacketPlayOutEntityMetadata(item.getId(), item.getDataWatcher(), true);
        // Create velocity paket - velocity is a Vector
        PacketPlayOutEntityVelocity velo = new PacketPlayOutEntityVelocity(item.getId(), new Vec3D(velocity.getX(), velocity.getY(), velocity.getZ()));

        ((CraftPlayer) p).getHandle().b.sendPacket(spawnItem);
        ((CraftPlayer) p).getHandle().b.sendPacket(velo);
        ((CraftPlayer) p).getHandle().b.sendPacket(data);

//        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
//            @Override
//            public void run() {
//                // Remove Item after 40 ticks (2 seconds)
//                PacketPlayOutEntityDestroy destroyItem = new PacketPlayOutEntityDestroy(item.getId());
//
//                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(destroyItem);
//            }
//        }, 40L);
    }
}
