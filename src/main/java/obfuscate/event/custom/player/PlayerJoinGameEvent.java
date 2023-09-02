package obfuscate.event.custom.player;

import obfuscate.event.custom.team.PlayerJoinRosterEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class PlayerJoinGameEvent extends PlayerJoinRosterEvent
{
    private final boolean spectates;

    public PlayerJoinGameEvent(Game game, StrikePlayer player, boolean spectates)
    {
        super(game, player, game.getPlayerRoster(player));
        this.spectates = spectates;
    }

    public boolean isSpectator() {
        return spectates;
    }
}
