package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;

public class BotKickCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {
        ctx.getSender().getPlayer().getGame().removeBots();
        return true;
    }
}
