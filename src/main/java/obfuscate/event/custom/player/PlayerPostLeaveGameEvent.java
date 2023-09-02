package obfuscate.event.custom.player;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class PlayerPostLeaveGameEvent extends CustomEvent {
    private final Game Game;
    private final StrikePlayer player;

    public PlayerPostLeaveGameEvent(Game Game, StrikePlayer player) {
        this.Game = Game;
        this.player = player;
    }

    public Game getGame() {
        return Game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
