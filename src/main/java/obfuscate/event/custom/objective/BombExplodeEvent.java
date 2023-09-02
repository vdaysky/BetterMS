package obfuscate.event.custom.objective;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.mechanic.item.objective.Bomb;

public class BombExplodeEvent extends CustomEvent
{
    private Game Game;
    private Bomb bomb;

    public BombExplodeEvent(Game Game, Bomb bomb) {
        this.Game = Game;
        this.bomb = bomb;
    }

    public Game getGame() {
        return Game;
    }

    public Bomb getBomb() {
        return bomb;
    }
}
