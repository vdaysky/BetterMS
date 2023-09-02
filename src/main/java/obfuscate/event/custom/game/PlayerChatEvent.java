package obfuscate.event.custom.game;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

/** triggered when player sends message to chat.
 * Originally passed to lobby but has computed game property which allows to pass event to game as well. */
public class PlayerChatEvent extends CancellableEvent {

    private String message;
    private StrikePlayer player;
    private Game game;

    public PlayerChatEvent(Game game, StrikePlayer player, String message) {
        this.message = message;
        this.player = player;
        this.game = game;
    }

    public String getMessage() {
        return message;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public Game getGame() {
        return game;
    }

}
