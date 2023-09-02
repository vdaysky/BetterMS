package obfuscate.event.custom.pause;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Pause;
import obfuscate.game.core.Game;

public class PauseEndEvent extends CustomEvent
{
    private Pause pauseType;
    private Game Game;

    public Pause getPauseType() {
        return pauseType;
    }

    public Game getGame() {
        return Game;
    }

    public PauseEndEvent(Pause pauseType, Game Game) {
        this.pauseType = pauseType;
        this.Game = Game;
    }
}
