package obfuscate.event.custom.player;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class PlayerPreSpectateEvent extends CustomEvent
{
    private Game Game;
    private StrikePlayer player;

    public PlayerPreSpectateEvent(Game Game, StrikePlayer player)
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
