package obfuscate.event.custom.player;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

/** Triggered when player joins the game. Extended by reconnect event so both can be handled with cascade listener
 * @see PlayerPostReconnectEvent */
public class PlayerPostGameJoinEvent extends CustomEvent
{
    private Game Game;
    private StrikePlayer player;

    private boolean spectates;

    public PlayerPostGameJoinEvent(Game Game, StrikePlayer player, boolean spectates) {
        this.Game = Game;
        this.player = player;
        this.spectates = spectates;
    }

    public Game getGame() {
        return Game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
    public boolean isSpectator() {
        return spectates;
    }
}
