package obfuscate.event.custom.player;

import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.network.annotations.Backend;
import obfuscate.util.serialize.load.Loadable;

/** Triggered by the backend when player was removed from the game.
 * <br/><br/> <b>Note:</b> <i>VALUE OF player.getPlayer() MIGHT BE NULL AS THE PLAYER IS NO LONGER ONLINE
 * AT THE MOMENT OF EVENT TRIGGERING</i>
 * */
@Backend
public class PlayerLeaveGameEvent extends FromBackendEvent {

    @Loadable(field = "game")
    private Game game;

    @Loadable(field = "player")
    private StrikePlayer player;

    public PlayerLeaveGameEvent(Object data) {
        super(data);
    }

    public Game getGame() {
        return game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
