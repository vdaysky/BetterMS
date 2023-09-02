package obfuscate.event.custom.player;

import obfuscate.game.player.StrikePlayer;

public class TabPrefixUpdateEvent extends TabNameUpdateEvent {

    private String prefix;

    public TabPrefixUpdateEvent(String prefix, String tabname, StrikePlayer player) {
        super(tabname, player);
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
