package obfuscate.event.custom.item;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.StrikeStack;

import javax.annotation.Nullable;

public class ItemDropEvent extends CancellableEvent
{
    private final StrikeItem item;
    @Nullable
    private final StrikeStack stack;
    private final Game Game;
    private final StrikePlayer holder;

    private final DropReason reason;

    public ItemDropEvent(Game Game, StrikeItem item, @Nullable StrikeStack stack, StrikePlayer holder, DropReason reason)
    {
        this.item = item;
        this.Game = Game;
        this.holder = holder;
        this.stack = stack;
        this.reason = reason;
    }

    public Game getGame() {
        return Game;
    }

    public StrikeItem getItem() {
        return item;
    }

    public StrikePlayer getHolder() {
        return holder;
    }

    @Nullable
    public StrikeStack getStack() {
        return stack;
    }

    public DropReason getReason() {
        return reason;
    }
}
