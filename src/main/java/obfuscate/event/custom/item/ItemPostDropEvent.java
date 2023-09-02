package obfuscate.event.custom.item;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.StrikeStack;

import javax.annotation.Nullable;

public class ItemPostDropEvent extends CustomEvent
{
    @Nullable
    private final StrikeStack stack;
    private final Game Game;
    private final StrikePlayer holder;

    private final DropReason reason;

    private final StrikeItem item;

    public ItemPostDropEvent(Game Game, StrikeItem item, @Nullable StrikeStack stack, StrikePlayer holder, DropReason reason)
    {
        this.stack = stack;
        this.Game = Game;
        this.holder = holder;
        this.reason = reason;
        this.item = item;
    }

    public Game getGame() {
        return Game;
    }

    @Nullable
    public StrikeStack getStack() {
        return stack;
    }

    public StrikePlayer getHolder() {
        return holder;
    }

    public DropReason getReason() {
        return reason;
    }

    public StrikeItem getItem() {
        return item;
    }
}
