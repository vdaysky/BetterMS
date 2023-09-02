package obfuscate.event.custom.player;

import obfuscate.event.Tracked;
import obfuscate.event.custom.CustomEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.dump.Serializable;

/** Triggered after player is fully initialized, meaning a bit later than player actually joined */
@Tracked
public class PlayerJoinServerEvent extends CustomEvent {

    @Serializable
    private final StrikePlayer player;

    public PlayerJoinServerEvent(StrikePlayer player) {
        this.player = player;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
