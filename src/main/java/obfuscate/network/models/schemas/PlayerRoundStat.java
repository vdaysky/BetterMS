package obfuscate.network.models.schemas;

import obfuscate.event.custom.network.ModelEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;

public class PlayerRoundStat extends SyncableObject {

    @Loadable(field = "player")
    private StrikePlayer player;

    @Loadable(field = "kills")
    private Integer kills;

    @Override
    public Class<? extends ModelEvent<PlayerRoundStat>> getFulfilledEvent() {
        return null;
    }
}
