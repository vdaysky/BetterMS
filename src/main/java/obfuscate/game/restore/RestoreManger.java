package obfuscate.game.restore;

import obfuscate.util.UtilMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class RestoreManger
{
    private final HashMap<Block, BlockRestoreData> _blocks = new HashMap<>();

    /* Restore all expired blocks */
    public void tickUpdate()
    {
        ArrayList<Block> toRemove = new ArrayList<>();

        for (BlockRestoreData blockRestoreData : _blocks.values()) {
            if (blockRestoreData.expires()) {
                blockRestoreData.restore();
                toRemove.add(blockRestoreData._block);
            }
        }
        //Remove Handled
        for (Block cur : toRemove)
            _blocks.remove(cur);
    }

    /* Restore specific block manually */
    public void Restore(Block block)
    {
        if (!Contains(block))
            return;
        _blocks.remove(block).restore();
    }

    /* Restore all blocks manually */
    public void RestoreAll()
    {
        for (BlockRestoreData data : _blocks.values())
            data.restore();

        _blocks.clear();
    }

    public HashSet<Location> RestoreBlockAround(Material type, Location location, int radius)
    {
        HashSet<Location> restored = new HashSet<Location>();

        Iterator<Block> blockIterator = _blocks.keySet().iterator();

        while (blockIterator.hasNext())
        {
            Block block = blockIterator.next();

            if (block.getType() != type)
                continue;

            if (UtilMath.offset(block.getLocation().add(0.5, 0.5, 0.5), location) > radius)
                continue;

            restored.add(block.getLocation().add(0.5, 0.5, 0.5));

            _blocks.get(block).restore();

            blockIterator.remove();
        }

        return restored;
    }

    public void MakeRevertibleChange(Block block, Material toMaterial, long expireTime)
    {
        // TODO: check if we actually replace data and not create new one
        if (!Contains(block)) {
            GetBlocks().put(block, new BlockRestoreData(block, block.getBlockData(), expireTime));
        }
        else {
            GetData(block).update(expireTime);
        }

        block.setType(toMaterial);
    }

    public boolean Contains(Block block)
    {
        if (GetBlocks().containsKey(block))
            return true;
        return false;
    }

    public BlockRestoreData GetData(Block block)
    {
        if (_blocks.containsKey(block))
            return _blocks.get(block);
        return null;
    }

    public HashMap<Block, BlockRestoreData> GetBlocks()
    {
        return _blocks;
    }

}
