package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.IntentEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class PlayerGenerateCodeIntent extends IntentEvent {

    @Serializable
    StrikePlayer player;

    public PlayerGenerateCodeIntent(StrikePlayer player) {
        this.player = player;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

}
