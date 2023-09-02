package obfuscate.permission;

public enum Permission
{
    OPERATOR("bms.operator"),
    MANAGE_GAMES("bms.games.manager"), // can manage games by subbing players, starting / restarting games, creating new instances
    CONFIG_GAME("bms.games.config"),
    TEAM_LEADER("bms.team.leader"),
    COACH("bms.team.coach"),

    MAINTAINER("bms.games.maintainer"), // can access map editor

    NPC("bms.practice.npc"),
    DEBUG("bms.dev.debug"), // useful debug commands such as hitreg debug, model inspect and so on
    ANY("bms.player.any") // default permission, given to all players
    ;

    String permission;
    Permission(String permission)
    {
        this.permission = permission;
    }
    public String getName() {
        return permission;
    }

    public static Permission getPermissionByName(String name)
    {
        for(Permission perm : values())
        {
            if(perm.getName().equalsIgnoreCase(name)){
                return perm;
            }
        }
        return null;
    }

    /**
     * game.match.can_pause matches game.*
     * */
    public boolean matches(String presentPermission) {
        String[] hasParts = presentPermission.split("\\.");
        String[] requestedParts = getName().split("\\.");

        for (int i = 0; i < Math.min(requestedParts.length, hasParts.length); i++) {

            if (hasParts[i].equals("*")) {
                // present permission is a wildcard, matches anything after
                return true;
            }

            if (!requestedParts[i].equals(hasParts[i])) {
                // mismatch
                return false;
            }
        }
        return requestedParts.length == hasParts.length;
    }
}
