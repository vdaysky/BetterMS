package obfuscate.event.custom.round;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class RoundStartEvent extends CustomEvent
{
    private Game Game;

    public Game getGame() {
        return Game;
    }

    public RoundStartEvent(Game Game) {
        this.Game = Game;
    }
}
