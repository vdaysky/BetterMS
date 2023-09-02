package obfuscate.event.custom.round;

import obfuscate.event.Tracked;
import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.core.TeamGame;
import obfuscate.team.InGameTeamData;
import obfuscate.util.serialize.dump.Serializable;

@Tracked
public class RoundWinEvent extends CustomEvent
{
    public enum Reason {
        TIME_RUN_OUT,
        OBJECTIVE,
        ELIMINATION

        ;
    }

    @Serializable
    private final Game game;

    @Serializable
    private final InGameTeamData winner;

    @Serializable
    private final InGameTeamData looser;

    @Serializable
    private final Integer roundNumber;

    @Serializable
    private final Reason reason;

    public RoundWinEvent(TeamGame game, InGameTeamData winner, Integer roundNumber, Reason reason)
    {
        this.game = game;
        this.winner = winner;
        this.looser = game.getRoster(winner.getTeam().getOpposite());
        this.roundNumber = roundNumber;
        this.reason = reason;
    }

    public Game getGame() {
        return game;
    }

    public InGameTeamData getWinner() {
        return winner;
    }

    public InGameTeamData getLooser() {
        return looser;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public Reason getReason() {
        return reason;
    }
}
