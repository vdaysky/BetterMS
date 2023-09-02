package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.TeamGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.message.MsgSender;

public class SwapCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        TeamGame teamGame = (TeamGame) ctx.getSender().getPlayer().getGame();
        teamGame.swapSides();
        ctx.getSender().sendMessage(MsgSender.GAME, "You swapped sides.");

        // respawn players at their new side
        for (StrikePlayer teamPlayer : teamGame.getOnlinePlayers())
        {
            teamGame.respawn(teamPlayer, false);
        }
        return false;
    }
}
