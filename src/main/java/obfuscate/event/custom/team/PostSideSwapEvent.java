package obfuscate.event.custom.team;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

/** Hello future me. If you want to trigger some "PlayerJoinRosterEvent" or something here - don't do it.
 * Roster is the same. It's only the team variable that changed. Do not overcomplicate things */
public class PostSideSwapEvent extends CustomEvent
{
    private Game game;

    public Game getGame() {
        return game;
    }

    public PostSideSwapEvent(Game game) {
        this.game = game;
    }
}
