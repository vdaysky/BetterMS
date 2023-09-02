package obfuscate.event.custom.lobby;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.hub.Hub;

public class PlayerLeaveHubEvent extends CustomEvent
{
    private StrikePlayer player;
    private Hub hub;

    public PlayerLeaveHubEvent(Hub lobby, StrikePlayer player) {
        this.player = player;
        this.hub = lobby;
    }

    public Hub getHub() {
        return hub;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
