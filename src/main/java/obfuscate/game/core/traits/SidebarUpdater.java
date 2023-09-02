package obfuscate.game.core.traits;

import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.util.sidebar.Sidebar;

public interface SidebarUpdater {

    void update(Competitive game, Sidebar sidebar, StrikePlayer player);

}
