package obfuscate.game.core.traits;

import obfuscate.game.core.plugins.GunGamePlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.util.chat.C;
import obfuscate.util.sidebar.Sidebar;

public class GunGameSidebarUpdater implements SidebarUpdater {

    GunGamePlugin gunGame;

    public GunGameSidebarUpdater(GunGamePlugin gunGame) {
        this.gunGame = gunGame;
    }

    @Override
    public void update(Competitive game, Sidebar sidebar, StrikePlayer player) {

        var plugin = gunGame;

        var level = plugin.getLevel(player);
        var levelKills = plugin.getLevelKills(player);
        var myLevelName = plugin.getLevelName(level);
        var nextLevelRequiredKills = plugin.levelKillRequirement(level + 1);

        var leaders = plugin.getLeaders();
        sidebar.setTitle(C.cGold + C.Bold + "Gun Game");

        sidebar.setLine(15, C.cGreen + C.Bold + "Your Level:");
        sidebar.setLine(14, C.cGreen + C.Bold + C.cYellow + C.Bold + level + " (" + myLevelName + ")");
        sidebar.setLine(13, " ");

        if (plugin.isOnLastLevel(player)) {
            sidebar.setLine(12, C.cGreen + C.Bold + "No Next Level");
        } else {
            sidebar.setLine(12, C.cGreen + C.Bold + "Next Level: "+ plugin.getLevelName(level + 1) + " " + levelKills + "/" + nextLevelRequiredKills);
        }

        sidebar.setLine(11, "  ");

        if (leaders.size() == 0) {
            sidebar.setLine(10, C.cGreen + C.Bold + "Leader:");
            sidebar.setLine(9, C.cGreen + C.Bold + C.cYellow + C.Bold + "No Leader");
            sidebar.setLine(8, "   ");
        } else if (leaders.size() == 1) {
            if (leaders.get(0) == player) {
                sidebar.setLine(10, C.cGreen + C.Bold + "Leader: You");
                sidebar.setLine(9, "    ");
            } else {
                var leaderLevel = plugin.getLevel(leaders.get(0));
                sidebar.setLine(10, C.cGreen + C.Bold + "Leader: " +  leaders.get(0).getName());
                sidebar.setLine(9, "Level " + leaderLevel + " (" + plugin.getLevelName(leaderLevel) + ")");
            }
        } else {
            var leaderLevel = plugin.getLevel(leaders.get(0));
            sidebar.setLine(10, C.cGreen + C.Bold + "Leader: " + leaders.get(0).getName() + "(+" + (leaders.size() - 1) + ")");
            sidebar.setLine(9, "Level " + leaderLevel + " (" + plugin.getLevelName(leaderLevel) + ")");
        }
    }
}
