package obfuscate.event.custom.intent;

import obfuscate.event.Tracked;
import obfuscate.event.custom.IntentEvent;
import obfuscate.game.core.IGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class ChangePlayerWhitelistStatusAtGameIntent extends IntentEvent {

    @Serializable
    private final StrikePlayer player;

    @Serializable
    private final StrikePlayer manager;

    @Serializable
    private final IGame game;

    @Serializable
    private final boolean isWhitelisted;


    public ChangePlayerWhitelistStatusAtGameIntent(StrikePlayer player, StrikePlayer manager, IGame game, boolean isWhitelisted) {
        this.player = player;
        this.game = game;
        this.manager = manager;
        this.isWhitelisted = isWhitelisted;
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

    public boolean isWhitelisted() {
        return isWhitelisted;
    }
}
