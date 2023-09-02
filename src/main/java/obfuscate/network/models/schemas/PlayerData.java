package obfuscate.network.models.schemas;

import obfuscate.event.custom.network.ModelEvent;
import obfuscate.event.custom.network.PlayerDataReceivedEvent;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;

import java.util.UUID;

@Model(name = "player")
public class PlayerData extends SyncableObject {



    @Override
    public String getIdField() {
        if (objectId != null) {
            return super.getIdField();
        }
        return "uuid";
    }

    @Loadable(field = "uuid")
    protected UUID uuid;

    @Loadable(field = "elo")
    private Integer elo;

    @Loadable(field = "role")
    private RoleData role;

    @Loadable(field = "team")
    private TeamData team;

    public PlayerData(){}

    @Override
    public Class<? extends ModelEvent<? extends SyncableObject>> getFulfilledEvent() {
        return PlayerDataReceivedEvent.class;
    }

    public Integer getElo() {
        return elo;
    }

    public RoleData getRole() {
        return role;
    }

    public TeamData getTeam() {
        return team;
    }

    public UUID getUuid() {
        return uuid;
    }

}
