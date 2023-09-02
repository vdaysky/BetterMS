package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.IntentEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.dump.Serializable;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Tracked
public class CreateGameIntentEvent extends IntentEvent {

    @Serializable
    @Nullable
    private final String mapName;

    @Serializable
    private final String mode;

    @Serializable
    @Nullable
    private final StrikePlayer player;

    @Serializable
    @Nullable
    private ArrayList<String> tags;

    public CreateGameIntentEvent(@Nullable String mapName, String mode, @Nullable StrikePlayer player) {
        this.mapName = mapName;
        this.mode = mode;
        this.player = player;
    }

    public CreateGameIntentEvent(@Nullable String mapName, String mode, @Nullable StrikePlayer player, @Nullable ArrayList<String> tags) {
        this(mapName, mode, player);
        this.tags = tags;
    }

    public String getMapName() {
        return mapName;
    }

    public String getMode() {
        return mode;
    }
}
