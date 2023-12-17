package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.IntentEvent;
import obfuscate.game.core.IGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class ChangePlayerBlacklistStatusAtGameIntent extends IntentEvent {

    @Serializable
    private final StrikePlayer player;

    @Serializable
    private final StrikePlayer manager;

    @Serializable
    private final IGame game;

    @Serializable
    private final boolean isBlacklisted;

    @Serializable
    private final String reason;


    public ChangePlayerBlacklistStatusAtGameIntent(StrikePlayer player, StrikePlayer manager, IGame game, boolean isBlacklisted, String reason) {
        this.player = player;
        this.game = game;
        this.manager = manager;
        this.isBlacklisted = isBlacklisted;
        this.reason = reason;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public StrikePlayer getManager() {
        return manager;
    }

    public IGame getGame() {
        return game;
    }

    public boolean isBlacklisted() {
        return isBlacklisted;
    }

    public String getReason() {
        return reason;
    }
}
