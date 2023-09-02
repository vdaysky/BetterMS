package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.TeamGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.message.MsgSender;
import obfuscate.team.InGameTeamData;
import obfuscate.util.time.Task;

public class SubCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {
        TeamGame teamGame = (TeamGame) ctx.getSender().getPlayer().getGame();

        String ign = ctx.getRequired("IGN");
        String team = ctx.getRequired("team");

        StrikePlayer player = teamGame.getPlayer(ign);

        if (player == null)
        {
            ctx.getSender().sendMessage(MsgSender.GAME, "Player " + ign + " not found in this game");
            return false;
        }

        if (!teamGame.canJoin(player, false)) {
            ctx.getSender().sendMessage(MsgSender.GAME, "Player " + ign + " can't join this game.");
            return false;
        }

        InGameTeamData roster = teamGame.getPlayerRoster(player);
        InGameTeamData newRoster = teamGame.getRosterByShortName(team);

        if (newRoster == null) {
            ctx.getSender().sendMessage(MsgSender.GAME, "Roster " + team + " does not exist! Rosters are: " +
                    teamGame.getRosters()[0].getName() + " and " + teamGame.getRosters()[1].getName());
            return false;
        }

        // if player is spectating even if he was on that roster already don't swear here,
        // respawn as participant to make this command look nice
        if (!player.isSpectating(teamGame) && roster == newRoster)
        {
            ctx.getSender().sendMessage(MsgSender.GAME, "Player is already playing on that team");
            return false;
        }

        teamGame.changePlayerTeam(player, newRoster.getTeam()).thenSync(
                x -> {
                    // synchronize with main thread
                    new Task(
                            () -> {
                                teamGame.respawn(player, false);
                                ctx.getSender().sendMessage(MsgSender.GAME, "Set " + player.getName() + "'s roster to " + newRoster.getName());
                            }, 0
                    ).run();

                    return x;
                }
        );

        return true;
    }
}
