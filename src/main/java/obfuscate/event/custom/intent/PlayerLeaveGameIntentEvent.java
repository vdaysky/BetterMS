package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.IntentEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class PlayerLeaveGameIntentEvent extends IntentEvent {

    @Serializable
    private final Game game;

    @Serializable
    private final StrikePlayer player;

    public PlayerLeaveGameIntentEvent(Game game, StrikePlayer player) {
        this.game = game;
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
