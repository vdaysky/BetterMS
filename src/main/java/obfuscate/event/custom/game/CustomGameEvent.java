package obfuscate.event.custom.game;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.util.serialize.dump.Serializable;

public abstract class CustomGameEvent extends CustomEvent
{
    @Serializable
    private Game game;

    public CustomGameEvent(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
