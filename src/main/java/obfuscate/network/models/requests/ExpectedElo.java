package obfuscate.network.models.requests;

import obfuscate.util.serialize.load.Struct;
import obfuscate.util.serialize.load.Loadable;

public class ExpectedElo extends Struct {

    @Loadable(field = "team_a_win")
    Integer teamAWin;

    @Loadable(field = "team_b_win")
    Integer teamBWin;

    @Loadable(field = "team_a_loss")
    Integer teamALoss;

    @Loadable(field = "team_b_loss")
    Integer teamBLoss;

    public Integer getTeamAWin() {
        return teamAWin;
    }

    public Integer getTeamBWin() {
        return teamBWin;
    }

    public Integer getTeamALoss() {
        return teamALoss;
    }

    public Integer getTeamBLoss() {
        return teamBLoss;
    }
}
