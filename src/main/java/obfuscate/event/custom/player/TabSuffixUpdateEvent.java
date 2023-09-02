package obfuscate.event.custom.player;

import obfuscate.game.player.StrikePlayer;

public class TabSuffixUpdateEvent extends TabNameUpdateEvent {

    private String suffix;

    public TabSuffixUpdateEvent(String suffix, String tabname, StrikePlayer player) {
        super(tabname, player);
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
