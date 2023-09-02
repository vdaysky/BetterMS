package obfuscate.event.custom.item;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;

public class ItemFocusEvent extends CustomEvent
{
    private final StrikeItem _item;
    private final StrikePlayer _holder;
    private final Game _game;

    public ItemFocusEvent(StrikeItem item, Game game, StrikePlayer holder) {
        _item = item;
        _holder = holder;
        _game = game;

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
}
