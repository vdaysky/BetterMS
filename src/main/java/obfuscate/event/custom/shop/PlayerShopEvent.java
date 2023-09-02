package obfuscate.event.custom.shop;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;

public class PlayerShopEvent extends CancellableEvent {
    private Game game;
    private StrikeItem boughtItem;
    private boolean wasFree;
    private StrikePlayer player;


    public PlayerShopEvent(Game game, StrikeItem boughtItem, StrikePlayer buyer, boolean wasFree) {
        this.game = game;
        this.boughtItem = boughtItem;
        this.wasFree = wasFree;
        this.player = buyer;
    }

    public Game getGame() {
        return game;
    }

    public StrikeItem getBoughtItem() {
        return boughtItem;
    }

    public boolean wasFree() {
        return wasFree;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

}
