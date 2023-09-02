package obfuscate.event.custom.item;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;

public class ItemLostFocusEvent extends CustomEvent
{
    private final StrikeItem _item;
    private final StrikePlayer _holder;
    private final Game _game;
    private final FocusLostReason _reason;

    public ItemLostFocusEvent(StrikeItem item, Game game, StrikePlayer holder, FocusLostReason reason) {
        _item = item;
        _holder = holder;
        _game = game;
        _reason = reason;
    }

    public StrikeItem getItem() {
        return _item;
    }

    public StrikePlayer getHolder() {
        return _holder;
    }

    public Game getGame() {
        return _game;
    }

    public FocusLostReason getReason() {
        return _reason;
    }

    public enum FocusLostReason {
        DROP,
        SWITCH,
        DEATH,
        CUSTOM,
        CONSUME
    }
}
