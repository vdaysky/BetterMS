package obfuscate.event.custom.gamestate;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class RoundTimerEndEvent extends CustomEvent {

    private Game game;

    public RoundTimerEndEvent(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

}
