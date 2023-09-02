package obfuscate.mechanic.item.utility;

import obfuscate.game.core.Game;
import obfuscate.game.core.GameSession;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.ItemStats;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.StrikeStack;

public abstract class ConsumableItem extends StrikeItem
{
    public ConsumableItem(ItemStats stat) {
        super(stat);
    }

    @Override
    public boolean canPickup(Game game, StrikePlayer player)
    {
        GameSession session = game.getGameSession(player);
        int slot = getSlot();

        if (!session.getInventory().hasItem(slot))
            return true;

        StrikeStack stack = session.getInventory().getStack(slot);

        return stack.getCount() < stack.top().getMaxStackSize();
    }

    public abstract int getMaxStackSize();


}
