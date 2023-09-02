package obfuscate.event.custom.network;

import obfuscate.game.player.StrikePlayer;
import obfuscate.network.models.schemas.PlayerData;

public class PlayerDataReceivedEvent extends ModelEvent<PlayerData> {

    private final StrikePlayer player;

    public PlayerDataReceivedEvent(PlayerData data /* this is player actually */) {
        super(data);
        this.player = (StrikePlayer) data;

    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
