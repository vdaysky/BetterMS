package obfuscate.network.models.schemas;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.network.ModelEvent;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;
import obfuscate.permission.Permission;

import java.util.List;
import java.util.Objects;

@Model(name = "role")
public class RoleData extends SyncableObject {

    public static RoleData BOT = new RoleData();

    @Loadable(field = "name")
    private String name;

    @Loadable(field = "tab_prefix")
    private String tabPrefix;

    // tab name color
    @Loadable(field = "tab_color")
    private String tabColor;

    // chat prefix
    @Loadable(field = "chat_prefix")
    private String chatPrefix;

    // chat suffix
    @Loadable(field = "chat_suffix")
    private String chatSuffix;

    // chat name color
    @Loadable(field = "chat_color")
    private String chatColor;

    //chat message color
    @Loadable(field = "chat_message_color")
    private String chatMessageColor;

    // team override name color
    @Loadable(field = "team_override_color")
    private Boolean teamOverrideColor;

    // permissions
    @Loadable(field = "permissions")
    private List<PermissionData> permissions;

    public String getName() {
        return name;
    }

    public String getTabPrefix() {
        return tabPrefix;
    }

    public String getTabColor() {
        return tabColor;
    }

    public String getChatPrefix() {
        return chatPrefix;
    }

    public String getChatSuffix() {
        return chatSuffix;
    }

    public String getChatColor() {
        return chatColor;
    }

    public String getChatMessageColor() {
        return chatMessageColor;
    }

    public Boolean getTeamOverrideColor() {
        return Objects.requireNonNullElse(teamOverrideColor, false);
    }

    public Boolean isImmuneToKnife() {
        return false;
    }

    public List<PermissionData> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission requestedPerm) {
        for (PermissionData presentPerm : getPermissions()) {
            if (requestedPerm.matches(presentPerm.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<? extends ModelEvent<RoleData>> getFulfilledEvent() {
        return null;
    }
}
