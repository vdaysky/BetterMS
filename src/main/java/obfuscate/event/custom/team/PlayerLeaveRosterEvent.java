package obfuscate.event.custom.team;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.team.InGameTeamData;

public class PlayerLeaveRosterEvent extends CustomEvent {

    private Game Game;
    private StrikePlayer player;
    private InGameTeamData team;

    public PlayerLeaveRosterEvent(Game Game, StrikePlayer player, InGameTeamData team) {
        this.Game = Game;
        this.player = player;
        this.team = team;
    }

    public Game getGame() {
        return Game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public InGameTeamData getRoster() {
        return team;
    }
}
