package obfuscate.network.models.schemas;

import obfuscate.event.custom.network.ModelEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;

import java.util.List;

@Model(name = "team")
public class TeamData extends SyncableObject {

    @Loadable(field = "short_name")
    private String shortName;

    @Loadable(field = "full_name")
    private String name;

    @Loadable(field = "location")
    private String location;

    private List<StrikePlayer> members;

    public String getShortName() {
        return shortName;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public Class<? extends ModelEvent<TeamData>> getFulfilledEvent() {
        return null;
    }
}
