package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.TeamGame;
import obfuscate.message.MsgSender;

public class PauseCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        try {
            TeamGame game = (TeamGame) ctx.getSender().getPlayer().getGame();
            game.setTecPause();
            ctx.getSender().sendMessage(MsgSender.GAME, "Added technical pause to queue.");
        } catch (Exception e) {
            ctx.getSender().sendMessage(MsgSender.CMD, "failed to pause the game.");
        }
        return false;
    }
}
