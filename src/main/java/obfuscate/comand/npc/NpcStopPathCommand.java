package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.npc.BotManager;
import obfuscate.game.player.StrikePlayer;
import obfuscate.message.MsgSender;

public class NpcStopPathCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        StrikePlayer player = ctx.getSender().getPlayer();

        String toStop = (String) ctx.getRequired("name");
        if (!BotManager.isPathActive(toStop))
        {
            player.sendMessage(MsgSender.NPC_MANAGER, "path " + toStop + " is not active");
            return false;
        }
        BotManager.stopPath(toStop);
        return true;
    }
}
