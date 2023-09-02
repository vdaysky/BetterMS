package obfuscate.event.custom.player;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.player.StrikePlayer;

public class TabNameUpdateEvent extends CustomEvent {
    private String tabname;
    private StrikePlayer player;

    public TabNameUpdateEvent(String tabname, StrikePlayer player) {
        this.tabname = tabname;
        this.player = player;
    }

    public String getTabName() {
        return tabname;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
