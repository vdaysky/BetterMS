package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.IntentEvent;
import obfuscate.game.core.Game;
import obfuscate.game.core.IGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class PlayerLeaveGameIntentEvent extends IntentEvent {

    @Serializable
    private final IGame game;

    @Serializable
    private final StrikePlayer player;

    public PlayerLeaveGameIntentEvent(IGame game, StrikePlayer player) {
        this.game = game;
        this.player = player;
    }

    public IGame getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
