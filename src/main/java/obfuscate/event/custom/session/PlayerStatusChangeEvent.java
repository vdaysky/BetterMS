package obfuscate.event.custom.session;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.core.PlayerStatus;
import obfuscate.game.player.StrikePlayer;

public class PlayerStatusChangeEvent extends CustomEvent
{
    private Game Game;
    private StrikePlayer player;
    private PlayerStatus status;

    public Game getGame() {
        return Game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public PlayerStatusChangeEvent(Game Game, StrikePlayer player, PlayerStatus status) {
        this.Game = Game;
        this.player = player;
        this.status = status;
    }
}
