package obfuscate.event.custom.intent;

import obfuscate.event.custom.IntentEvent;
import obfuscate.game.core.Game;
import obfuscate.game.core.IGame;
import obfuscate.util.serialize.dump.Serializable;

public class GameDeleteIntentEvent extends IntentEvent {

    @Serializable
    private IGame game;

    public GameDeleteIntentEvent(IGame game) {
        this.game = game;
    }

    public IGame getGame() {
        return game;
    }
}
