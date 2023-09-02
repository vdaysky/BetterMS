package obfuscate.comand.server;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.message.MsgSender;

public class RPCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        ctx.getSender().sendMessage(MsgSender.SERVER, "started resource pack loading...");
        ctx.getSender().getPlayer().loadResources();
        return true;
    }
}
