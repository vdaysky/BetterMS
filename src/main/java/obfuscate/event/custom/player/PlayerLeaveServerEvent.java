package obfuscate.event.custom.player;

import obfuscate.event.Tracked;
import obfuscate.event.custom.CustomEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class PlayerLeaveServerEvent extends CustomEvent {
    @Serializable
    private final StrikePlayer player;

    public PlayerLeaveServerEvent(StrikePlayer player) {
        this.player = player;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
