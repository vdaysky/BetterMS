package obfuscate.event.custom.game;

import obfuscate.game.core.Game;

public class GameDestroyEvent extends CustomGameEvent
{
    public GameDestroyEvent(Game game) {
        super(game);
    }
}
