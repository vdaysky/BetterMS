package obfuscate.event.custom.ready;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;

public class ParticipantsReadyEvent extends CustomEvent
{
    private Game Game;

    public ParticipantsReadyEvent(Game Game)
    {
        this.Game = Game;
    }

    public Game getGame() {
        return Game;
    }
}
