package obfuscate.event.custom.time;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class TimeEvent extends CustomEvent
{
    UpdateReason reason;
    Game Game;

    public TimeEvent(Game Game, UpdateReason reason)
    {
        this.Game = Game;
        this.reason = reason;
    }

    public enum UpdateReason
    {
        TICK,
        SECOND;
    }


    public Game getGame() {
        return Game;
    }

    public UpdateReason getReason()
    {
        return reason;
    }
}
