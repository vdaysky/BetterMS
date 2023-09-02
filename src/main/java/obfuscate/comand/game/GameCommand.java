package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.message.MsgSender;

public class GameCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {
        if (ctx.getSender().getPlayer().getGame() == null) {
            ctx.getSender().sendMessage(MsgSender.CMD, "You have to be in-game to use this command.");
            return false;
        }
        return true;
    }
}
