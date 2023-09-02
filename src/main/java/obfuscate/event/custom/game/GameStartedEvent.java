package obfuscate.event.custom.game;

import obfuscate.event.Tracked;
import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class GameStartedEvent extends CustomEvent
{
    @Serializable
    private Game game;

    public GameStartedEvent(Game Game) {
        this.game = Game;
    }

    public Game getGame() {
        return game;
    }
}
