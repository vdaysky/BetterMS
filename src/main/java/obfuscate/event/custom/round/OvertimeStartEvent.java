package obfuscate.event.custom.round;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class OvertimeStartEvent extends CustomEvent
{
    private int overtimeIndex;
    private Game Game;

    public int getOvertimeIndex() {
        return overtimeIndex;
    }

    public Game getGame() {
        return Game;
    }

    public OvertimeStartEvent(int overtimeIndex, Game Game) {
        this.overtimeIndex = overtimeIndex;
        this.Game = Game;
    }
}
