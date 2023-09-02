package obfuscate.event.custom.backend.match;

import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.team.InGameTeamData;

public class MatchWinEvent extends FromBackendEvent {

    @Loadable(field = "winner")
    InGameTeamData winner;

    @Loadable(field = "looser")
    InGameTeamData looser;

    public MatchWinEvent(Object jsonEvt) {
        super(jsonEvt);
    }

    public InGameTeamData getWinner() {
        return winner;
    }

    public InGameTeamData getLooser() {
        return looser;
    }
}
