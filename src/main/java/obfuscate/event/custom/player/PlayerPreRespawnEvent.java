package obfuscate.event.custom.player;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

/** Triggered when participating player is being respawned */
public class PlayerPreRespawnEvent extends CustomEvent
{
    private Game Game;
    private StrikePlayer player;

    public PlayerPreRespawnEvent(Game Game, StrikePlayer player)
    {
        this.Game = Game;
        this.player = player;
    }

    public Game getGame()
    {
        return Game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
