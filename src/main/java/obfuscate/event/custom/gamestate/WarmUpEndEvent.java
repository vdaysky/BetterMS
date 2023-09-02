package obfuscate.event.custom.gamestate;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class WarmUpEndEvent extends CustomEvent {

    private final Game game;

    public WarmUpEndEvent(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

}
