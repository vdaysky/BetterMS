package obfuscate.event.custom.game;

import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.gamemode.Competitive;
import obfuscate.network.annotations.Backend;
import obfuscate.util.serialize.load.Loadable;

@Backend
public class GameTerminatedEvent extends FromBackendEvent {

    @Loadable(field = "game")
    private Competitive game;

    public GameTerminatedEvent(Object jsonEvt) {
        super(jsonEvt);
    }

    public Competitive getGame() {
        return game;
    }
}
