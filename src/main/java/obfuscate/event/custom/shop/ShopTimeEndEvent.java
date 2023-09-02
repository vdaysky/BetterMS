package obfuscate.event.custom.shop;

import obfuscate.game.core.Game;

public class ShopTimeEndEvent extends ShopUpdateEvent {

    public ShopTimeEndEvent(Game Game) {
        super(Game, false);
    }
}
