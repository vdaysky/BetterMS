package obfuscate.event.custom.game;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class GameInitEvent extends CustomEvent
{
    private Game Game;

    public GameInitEvent(Game Game) {
        this.Game = Game;
    }

    public Game getGame() {
        return Game;
    }
}
