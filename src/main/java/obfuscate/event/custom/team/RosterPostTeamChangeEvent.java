package obfuscate.event.custom.team;

import obfuscate.event.custom.CustomEvent;
import obfuscate.team.InGameTeamData;
import obfuscate.team.StrikeTeam;
import obfuscate.game.core.Game;

public class RosterPostTeamChangeEvent extends CustomEvent
{

    private InGameTeamData roster;
    private StrikeTeam team;
    private Game Game;

    public RosterPostTeamChangeEvent(Game Game, InGameTeamData roster, StrikeTeam team) {

        this.roster = roster;
        this.team = team;
        this.Game = Game;
    }

    public InGameTeamData getRoster() {
        return roster;
    }

    public StrikeTeam getTeam() {
        return team;
    }

    public Game getGame() {
        return Game;
    }
}
