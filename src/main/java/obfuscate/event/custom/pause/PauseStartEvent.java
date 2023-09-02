package obfuscate.event.custom.pause;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Pause;
import obfuscate.game.core.Game;

public class PauseStartEvent extends CustomEvent
{
    private Pause pauseType;
    private Game Game;

    public Pause getPauseType() {
        return pauseType;
    }

    public Game getGame() {
        return Game;
    }

    public PauseStartEvent(Pause pauseType, Game Game) {
        this.pauseType = pauseType;
        this.Game = Game;
    }
}
