package obfuscate.event.custom.player;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;

public class PlayerUseItemEvent extends CancellableEvent {
    private final StrikePlayer player;
    private final Game Game;
    private final StrikeItem item;

    public PlayerUseItemEvent(StrikePlayer player, Game Game, StrikeItem item) {
        this.player = player;
        this.Game = Game;
        this.item = item;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public Game getGame() {
        return Game;
    }

    public StrikeItem getItem() {
        return item;
    }
}
