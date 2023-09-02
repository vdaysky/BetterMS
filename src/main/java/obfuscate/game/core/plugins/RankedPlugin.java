package obfuscate.game.core.plugins;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalEvent;
import obfuscate.event.custom.game.GameStartedEvent;
import obfuscate.event.custom.player.PlayerJoinGameEvent;
import obfuscate.game.core.traits.RankedSidebarUpdater;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.message.MsgSender;
import obfuscate.network.BackendManager;
import obfuscate.network.models.requests.ExpectedElo;
import obfuscate.team.InGameTeamData;
import obfuscate.util.chat.C;

import javax.annotation.Nullable;

@Plugin
public class RankedPlugin implements IPlugin<Competitive> {

    ExpectedElo eloData = null;
    Competitive game;

    @LocalEvent
    private void onGameStartShowElo(GameStartedEvent e) {

        BackendManager Backend = MsdmPlugin.getBackend();

        Backend.getExpectedElo(e.getGame()).thenSync(
            eloData -> {
                this.eloData = eloData;
                displayEloData(e.getGame().getTeamA(), eloData.getTeamAWin(), eloData.getTeamALoss());
                displayEloData(e.getGame().getTeamB(), eloData.getTeamBWin(), eloData.getTeamBLoss());
                return null;
            }
        );
    }

    private void displayEloData(InGameTeamData roster, int winElo, int looseElo) {

        for (StrikePlayer player : roster) {
            player.sendMessage(
                    MsgSender.GAME,
                    "You are expected to receive " + C.cYellow + winElo + C.cWhite +
                            " elo in case of win or loose " + C.cYellow + looseElo + C.cWhite + " in case of loss."
            );
        }
    }

    @Override
    public void preInit(Competitive instance) {
        this.game = instance;
        game.getSharedContext().setSidebarUpdater(
            new RankedSidebarUpdater()
        );
    }

    public @Nullable ExpectedElo getEloData() {
        return eloData;
    }

    @LocalEvent
    private void onPlayerJoin(PlayerJoinGameEvent e) {
        var session = e.getGame().getGameSession(e.getPlayer());

        if (eloData == null) {
            return;
        }

        if (session.getRoster() == null) {
            return;
        }

        if (session.getRoster() == game.getTeamA()) {
            displayEloData(session.getRoster(), eloData.getTeamAWin(), eloData.getTeamALoss());
        } else {
            displayEloData(session.getRoster(), eloData.getTeamBWin(), eloData.getTeamBLoss());
        }
    }
}
