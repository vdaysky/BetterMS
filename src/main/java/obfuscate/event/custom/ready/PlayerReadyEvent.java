package obfuscate.event.custom.ready;


import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class PlayerReadyEvent extends CustomEvent
{
    private StrikePlayer player;
    private Game Game;

    public PlayerReadyEvent(Game Game, StrikePlayer player)
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
