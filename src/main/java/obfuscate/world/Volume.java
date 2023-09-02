package obfuscate.world;

import net.minecraft.core.BlockPosition;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Volume
{
    BlockPosition center; int x; int y; int z;

    public Volume(BlockPosition center, int x, int y, int z)
    {
        this.center = center;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isInside(Location loc)
    {
        Vector vec = new Vector(center.getX() - loc.getX(), center.getY() - loc.getY(), center.getZ() - loc.getZ());
        return Math.abs(vec.getX()) < x && Math.abs(vec.getY()) < y && Math.abs(vec.getZ()) < z;
    }
}
