package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.TeamGame;
import obfuscate.message.MsgSender;

public class UnpauseCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        TeamGame game = (TeamGame) ctx.getSender().getPlayer().getGame();

        if (game.getPauseType() != null) {
            game.unpause();
        }

        ctx.getSender().getPlayer().sendMessage(MsgSender.GAME, "You unpaused the game.");
        return false;
    }
}
