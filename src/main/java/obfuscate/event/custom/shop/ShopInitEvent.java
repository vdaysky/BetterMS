package obfuscate.event.custom.shop;

import obfuscate.game.core.Game;

public class ShopInitEvent extends ShopUpdateEvent{
    public ShopInitEvent(Game Game) {
        super(Game, true);
    }
}
