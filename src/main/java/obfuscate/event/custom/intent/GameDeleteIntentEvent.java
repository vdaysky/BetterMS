package obfuscate.event.custom.intent;

import obfuscate.event.custom.IntentEvent;
import obfuscate.game.core.Game;
import obfuscate.util.serialize.dump.Serializable;

public class GameDeleteIntentEvent extends IntentEvent {

    @Serializable
    private Game game;

    public GameDeleteIntentEvent(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
