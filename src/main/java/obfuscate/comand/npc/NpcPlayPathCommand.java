package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.npc.BotManager;
import obfuscate.game.npc.PrerecordedPath;
import obfuscate.game.player.StrikePlayer;
import obfuscate.message.MsgSender;
import obfuscate.world.GameMap;
import net.citizensnpcs.api.npc.NPC;

public class NpcPlayPathCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        StrikePlayer player = ctx.getSender().getPlayer();
        String path_name = ctx.getRequired("name");
        GameMap map = player.getGame().getGameMap();
        PrerecordedPath path = PrerecordedPath.load(map, path_name);

        if (path == null)
        {
            player.sendMessage(MsgSender.NPC_MANAGER, "No path " + path_name + " found for map " + map.getName());
            return false;
        }

        if (BotManager.isPathActive(path.getPathName()))
        {
            player.sendMessage(MsgSender.NPC_MANAGER, "Path is already being run");
            return false;
        }

        // everything is fine, create bot
        NPC bot = BotManager.createRunnerBot(player.getGame(), path);
        return true;
    }
}
