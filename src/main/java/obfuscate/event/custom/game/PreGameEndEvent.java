package obfuscate.event.custom.game;

import obfuscate.event.Tracked;
import obfuscate.game.core.Game;
import obfuscate.util.serialize.dump.Serializable;
import obfuscate.team.InGameTeamData;

/** This event is called immediately after team wins a game. */
@Tracked
public class PreGameEndEvent extends CustomGameEvent
{
    @Serializable
    private final InGameTeamData winner;

    @Serializable
    private final InGameTeamData looser;

    public PreGameEndEvent(Game game, InGameTeamData winner, InGameTeamData looser) {
        super(game);
        this.winner = winner;
        this.looser = looser;
    }

    public InGameTeamData getWinner() {
        return winner;
    }

    public InGameTeamData getLooser() {
        return looser;
    }
}
