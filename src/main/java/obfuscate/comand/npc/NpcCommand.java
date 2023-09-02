package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;

public class NpcCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {

        return ctx.getSender().getPlayer().getGame() != null;
    }
}
