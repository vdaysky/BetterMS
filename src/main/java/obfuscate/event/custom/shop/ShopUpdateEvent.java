package obfuscate.event.custom.shop;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;

public class ShopUpdateEvent extends CancellableEvent {
    private Game Game;
    private boolean shoppable;

    public ShopUpdateEvent(Game Game, boolean shoppable) {
        this.Game = Game;
    }

    public Game getGame() {
        return Game;
    }

    public boolean isShoppable() {
        return shoppable;
    }
}
