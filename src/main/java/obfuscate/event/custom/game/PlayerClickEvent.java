package obfuscate.event.custom.game;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class PlayerClickEvent extends CustomEvent {
    private final Game game;

    private final StrikePlayer player;

    private final boolean isRightClick;

    public PlayerClickEvent(Game game, StrikePlayer player, boolean isRightClick) {
        this.game = game;
        this.player = player;
        this.isRightClick = isRightClick;
    }

    public boolean isRightClick() {
        return isRightClick;
    }

    public Game getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
