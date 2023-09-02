package obfuscate.game.restore;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockRestoreData
{
    protected Block _block;

    protected BlockData _blockData;

    protected long _expiresAt;

    public BlockRestoreData(Block block, BlockData toData, long expireDelay)
    {
        _block = block;

        _blockData = toData;
        _expiresAt = System.currentTimeMillis() + expireDelay;
    }

    public boolean expires()
    {
        return System.currentTimeMillis() > _expiresAt;
    }

    /** if restore entry exists, we only have to postpone (or maybe speed up)
     * restoring. We don't store current block state, because it is temporary */
    public void update(long expireTime) {
        _expiresAt = System.currentTimeMillis() + expireTime;
    }

    public void restore() {
        _block.setBlockData(_blockData);
    }

}
