package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.npc.PrerecordedPath;
import obfuscate.game.player.StrikePlayer;

public class NpcDeletePathCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        StrikePlayer player = ctx.getSender().getPlayer();

        String toDelete = ctx.getRequired("name");
        if (PrerecordedPath.delete(player.getGame().getGameMap(), toDelete))
        {
            player.sendMessage("Successfully deleted " + toDelete);
        }
        else
        {
            player.sendMessage("Could not delete " + toDelete);
        }
        return true;
    }
}
