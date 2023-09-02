package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.dump.Serializable;
import obfuscate.team.StrikeTeam;

@Tracked
public class PlayerTeamChangeIntentEvent extends CustomEvent {

    @Serializable
    private Game game;

    @Serializable
    private StrikePlayer player;

    @Serializable
    private StrikeTeam team;

    public PlayerTeamChangeIntentEvent(Game game, StrikePlayer player, StrikeTeam team) {
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

    public StrikeTeam getTeam() {
        return team;
    }
}
