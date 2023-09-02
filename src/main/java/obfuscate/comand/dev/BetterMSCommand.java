package obfuscate.comand.dev;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.message.MsgSender;

public class BetterMSCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        ctx.getSender().sendMessage(MsgSender.CMD, "Deprecated");
        return true;
    }
}
