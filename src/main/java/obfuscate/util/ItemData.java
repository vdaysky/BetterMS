package obfuscate.util;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ItemData
{
    public Location loc;
    public Vector velocity;
    public ItemStack stack;

    public ItemData(Location loc, Vector velocity, ItemStack stack)
    {
        this.loc = loc;
        this.stack = stack;
        this.velocity = velocity;
    }

    public ItemData(Item item)
    {
        this.loc = item.getLocation();
        this.stack = item.getItemStack();
        this.velocity = item.getVelocity();
    }
}
