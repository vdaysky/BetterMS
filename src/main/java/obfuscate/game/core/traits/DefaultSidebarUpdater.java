package obfuscate.game.core.traits;

import obfuscate.game.config.ConfigField;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.util.chat.C;
import obfuscate.util.sidebar.Sidebar;
import obfuscate.util.time.Time;

public class DefaultSidebarUpdater implements SidebarUpdater {

    @Override
    public void update(Competitive game, Sidebar sidebar, StrikePlayer player) {

        var aScore = game.getTotalScore(game.getTeamA());
        var bScore = game.getTotalScore(game.getTeamB());

        var aName = game.getTeamA().getNiceName();
        var bName = game.getTeamB().getNiceName();

        var aColor = game.getTeamA().getTeam().getColor();
        var bColor = game.getTeamB().getTeam().getColor();

        var aAlive = game.getTeamA().getAlivePlayers().size();
        var bAlive = game.getTeamB().getAlivePlayers().size();

        var playingTo = game.getConfig().getValue(ConfigField.MAX_ROUNDS).val();

        sidebar.setTitle(C.cYellow + C.Bold + "BetterMS");

        sidebar.setLine(12, "");
        sidebar.setLine(11, aScore + " " + aColor + C.Bold + aName);
        sidebar.setLine(10, aAlive + " Alive ");
        sidebar.setLine(9, " ");
        sidebar.setLine(8, bScore + " " + bColor + C.Bold + bName);
        sidebar.setLine(7, bAlive + " Alive");
        sidebar.setLine(6, "  ");
        sidebar.setLine(5, C.cGold + C.Bold + "Playing To");
        sidebar.setLine(4, (playingTo / 2) + " Wins");
        sidebar.setLine(3, "   ");

        if (game.getBomb().isActive()) {
            if (game.getPhaseSecondsLeft() % 2 == 0) {
                sidebar.setLine(2, C.cGold + C.Bold + "Bomb Active");
                sidebar.setLine(1, "    ");
            } else {
                sidebar.setLine(2, C.cWhite + C.Bold + "Bomb Active");
                sidebar.setLine(1, "    ");
            }
        } else {
            sidebar.setLine(2, C.cGold + C.Bold + "Time Left");
            String time = Time.sFormatVerbose(game.getPhaseSecondsLeft());
            if (game.getGameState().isWarmup()) {
                time += " (Warmup)";
            }
            sidebar.setLine(1, time);
        }
    }
}

