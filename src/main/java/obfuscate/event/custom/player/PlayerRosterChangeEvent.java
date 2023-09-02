package obfuscate.event.custom.player;

import obfuscate.event.custom.team.PlayerJoinRosterEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.team.StrikeTeam;

public class PlayerRosterChangeEvent extends PlayerJoinRosterEvent
{
    private final Game game;
    private final StrikePlayer player;

    private final StrikeTeam newTeam;


    public PlayerRosterChangeEvent(Game game, StrikePlayer player, StrikeTeam joined) {
        super(game, player, game.getRoster(joined));
        this.game = game;
        this.player = player;
        this.newTeam = joined;
    }

    public Game getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public StrikeTeam getNewTeam() {
        return newTeam;
    }
}
