package obfuscate.event.custom.objective;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.objective.Bomb;

public class BombPlantStopEvent extends CustomEvent
{
    private StrikePlayer planter;
    private Game Game;
    private Bomb bomb;

    public BombPlantStopEvent(StrikePlayer planter, Game Game, Bomb bomb) {
        this.planter = planter;
        this.Game = Game;
        this.bomb = bomb;
    }

    public StrikePlayer getDefuser() {
        return planter;
    }

    public Game getGame() {
        return Game;
    }

    public Bomb getBomb() {
        return bomb;
    }
}
