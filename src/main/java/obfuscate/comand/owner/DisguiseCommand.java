package obfuscate.comand.owner;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;

public class DisguiseCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        if (ctx.getSender().getPlayer().isDisguised()) {
            ctx.getSender().getPlayer().sendTitle("You are not disguised anymore", "", 10, 60, 10);
        }
        else {
            ctx.getSender().getPlayer().sendTitle("You are now disguised", "", 10, 60, 10);
        }

        ctx.getSender().getPlayer().toggleDisguise();
        return false;
    }
}
