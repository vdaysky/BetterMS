package obfuscate.event.custom.gamestate;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class FreezeTimeEndEvent extends CustomEvent {

    private Game game;

    public FreezeTimeEndEvent(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

}
