package obfuscate.comand.team.leader;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.TeamGame;
import obfuscate.message.MsgSender;
import obfuscate.team.InGameTeamData;

public class TacPauseCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        InGameTeamData roster = ctx.getSender().getPlayer().getGame().getPlayerRoster(ctx.getSender().getPlayer());
        TeamGame teamGame = (TeamGame) ctx.getSender().getPlayer().getGame();

        if (!teamGame.hasPauses(roster))
        {
            ctx.getSender().sendMessage(MsgSender.GAME, "Your team does not have any pauses left.");
            return false;
        }

        if (teamGame.isPaused()) {
            ctx.getSender().sendMessage(MsgSender.GAME, "Game is already paused.");
            return false;
        }

        if (teamGame.hasNextPause()) {
            ctx.getSender().sendMessage(MsgSender.GAME, "Game already has pause queued.");
            return false;
        }

        teamGame.requestTacPause(roster);
        ctx.getSender().sendMessage(MsgSender.GAME, "You paused the game. pauses left: " + teamGame.getPausesLeft( roster ) );
        return true;
    }
}
