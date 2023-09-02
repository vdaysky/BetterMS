package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.npc.PrerecordedPath;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public class NpcPathsCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        StrikePlayer player = ctx.getSender().getPlayer();

        ArrayList<String> paths = PrerecordedPath.getPathNames(player.getGame().getGameMap());
        if (paths.isEmpty())
        {
            player.sendMessage(ChatColor.RED + "There are no paths available!");
        }
        else
        {
            player.sendMessage(ChatColor.GREEN + "There are " + ChatColor.YELLOW + paths.size() + ChatColor.GREEN + " paths available:");
        }
        for(String path : paths)
        {
            player.sendMessage("- " + path);
        }
        return true;
    }
}
