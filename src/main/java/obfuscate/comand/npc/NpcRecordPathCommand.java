package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.npc.Recorder;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.ChatColor;

public class NpcRecordPathCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        String pathname = ctx.getRequired("name");
        StrikePlayer player = ctx.getSender().getPlayer();

        Recorder recorder = new Recorder(player, pathname);
        boolean success = recorder.start();
        if (success)
        {
            player.sendMessage(ChatColor.GREEN + "Recording started!");
        }
        else
        {
            player.sendMessage(ChatColor.RED + "There is already recording in process!");
        }
        return true;
    }
}
