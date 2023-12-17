package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.IntentEvent;
import obfuscate.game.core.Game;
import obfuscate.game.core.IGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.team.StrikeTeam;
import obfuscate.util.serialize.dump.Serializable;

import javax.annotation.Nullable;

@Tracked
public class PlayerJoinGameIntentEvent extends IntentEvent {

    @Serializable
    private final IGame game;

    @Serializable
    private final StrikePlayer player;

    @Serializable
    private final StrikeTeam team;

    @Serializable
    private final boolean spectate;

    public PlayerJoinGameIntentEvent(IGame game, @Nullable StrikeTeam team, StrikePlayer player, boolean spec) {
        this.game = game;
        this.player = player;
        this.team = team;
        this.spectate = spec;
    }

    public boolean isSpectating() {
        return spectate;
    }

    public IGame getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public StrikeTeam getTeam() {
        return team;
    }
}
