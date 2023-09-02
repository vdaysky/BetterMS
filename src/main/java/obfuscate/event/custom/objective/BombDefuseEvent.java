package obfuscate.event.custom.objective;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.objective.Bomb;

public class BombDefuseEvent extends CustomEvent
{
    private StrikePlayer defuser;
    private Game Game;
    private Bomb bomb;

    public BombDefuseEvent(StrikePlayer defuser, Game Game, Bomb bomb) {
        this.defuser = defuser;
        this.Game = Game;
        this.bomb = bomb;
    }

    public StrikePlayer getDefuser() {
        return defuser;
    }

    public Game getGame() {
        return Game;
    }

    public Bomb getBomb() {
        return bomb;
    }
}
