package obfuscate.comand.npc;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.npc.Recorder;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.ChatColor;

public class NpcStopRecordCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        StrikePlayer player = ctx.getSender().getPlayer();
        Recorder recorder = Recorder.getActive(player);
        if (recorder == null)
        {
            player.sendMessage(ChatColor.RED + "Recording not initialized!");
            return false;
        }

        boolean success = recorder.stop();
        if (success)
        {
            player.sendMessage(ChatColor.GREEN + "Recording stopped!");
        }
        else
        {
            player.sendMessage(ChatColor.RED + "Recording task is null");
        }
        recorder.saveRecording();
        return true;
    }
}
