package obfuscate.event.custom.game;

import obfuscate.game.core.Game;

public class GameStartCancelEvent extends CustomGameEvent{

    public GameStartCancelEvent(Game game) {
        super(game);
    }
}
