package obfuscate.event.custom.backend.player;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.game.core.PlayerStatus;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.network.annotations.Backend;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.team.InGameTeamData;

@Backend
public class PlayerGameConnectEvent extends FromBackendEvent {

    @Loadable(field="game_id")
    private Competitive game;
    @Loadable(field="player_id")
    private StrikePlayer player;

    @Loadable(field = "team_id")
    private InGameTeamData team;

    @Loadable(field="status")
    private PlayerStatus status;

    public boolean isSpectating() {
        MsdmPlugin.highlight("PlayerGameConnectEvent.isSpectating: " + status);
        return status == PlayerStatus.SPECTATING;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public PlayerGameConnectEvent(Object json) {
        super(json);
    }

    public Competitive getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public InGameTeamData getTeam() {
        return team;
    }
}
