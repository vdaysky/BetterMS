package obfuscate.event.custom.team;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.team.InGameTeamData;

public class PlayerJoinRosterEvent extends CustomEvent
{
    private Game game;
    private StrikePlayer player;
    private InGameTeamData team;

    public PlayerJoinRosterEvent(Game game, StrikePlayer player, InGameTeamData team) {
        this.game = game;
        this.player = player;
        this.team = team;
    }

    public Game getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public InGameTeamData getRoster() {
        return team;
    }
}
