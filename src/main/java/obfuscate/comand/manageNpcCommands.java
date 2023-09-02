package obfuscate.comand;

import obfuscate.game.core.Game;
import obfuscate.game.npc.HitboxTest;
import obfuscate.game.npc.BotManager;
import obfuscate.game.npc.PrerecordedPath;
import obfuscate.game.npc.Recorder;
import obfuscate.game.player.StrikePlayer;
import obfuscate.message.MsgSender;
import obfuscate.world.GameMap;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class manageNpcCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        StrikePlayer admin = StrikePlayer.getOrCreate(((Player) commandSender));
        Game game = admin.getGame();

        if (!admin.getPlayer().isOp())return false;

        if (command.getName().equalsIgnoreCase("play") && args.length == 1)
        {
            if (game == null) {
                admin.sendMessage(MsgSender.NPC_MANAGER, "You have to be in game to use this command");
                return false;
            }

            GameMap map = game.getGameMap();
            String pathName = args[0];
            PrerecordedPath path = PrerecordedPath.load(map, pathName);

            if (path == null)
            {
                admin.sendMessage(MsgSender.NPC_MANAGER, "No path " + pathName + " found for map " + map.getName());
                return false;
            }

            if (BotManager.isPathActive(path.getPathName()))
            {
                admin.sendMessage(MsgSender.NPC_MANAGER, "Path is already being run");
                return false;
            }

            // everything is fine, create bot
            NPC bot = BotManager.createRunnerBot(game, path);
        }

        if (command.getName().equalsIgnoreCase("record") && args.length == 1)
        {
            String pathname = args[0];
            Recorder recorder = new Recorder(admin, pathname);
            boolean success = recorder.start();
            if (success)
            {
                admin.sendMessage(ChatColor.GREEN + "Recording started!");
            }
            else
            {
                admin.sendMessage(ChatColor.RED + "There is already recording in process!");
            }

        }

        if (command.getName().equalsIgnoreCase("rstop"))
        {
            Recorder recorder = Recorder.getActive(admin);
            if (recorder == null)
            {
                admin.sendMessage(ChatColor.RED + "Recording not initialized!");
                return false;
            }

            boolean success = recorder.stop();
            if (success)
            {
                admin.sendMessage(ChatColor.GREEN + "Recording stopped!");
            }
            else
            {
                admin.sendMessage(ChatColor.RED + "Recording task is null");
            }
            recorder.saveRecording();
        }

        if (command.getName().equalsIgnoreCase("paths"))
        {
            ArrayList<String> paths = PrerecordedPath.getPathNames(game.getGameMap());
            if (paths.isEmpty())
            {
                admin.sendMessage(ChatColor.RED + "There are no paths available!");
            }
            else
            {
                admin.sendMessage(ChatColor.GREEN + "There are " + ChatColor.YELLOW + paths.size() + ChatColor.GREEN + " paths available:");
            }
            for(String path : paths)
            {
                admin.sendMessage("- " + path);
            }
        }

        if (command.getName().equalsIgnoreCase("delete") && args.length == 1)
        {
            String toDelete = args[0];
            if (PrerecordedPath.delete(game.getGameMap(), toDelete))
            {
                admin.sendMessage("Successfully deleted " + toDelete);
            }
            else
            {
                admin.sendMessage("Could not delete " + toDelete);
            }
        }

        if (command.getName().equalsIgnoreCase("pstop") && args.length == 1)
        {
            String toStop = args[0];
            if (!BotManager.isPathActive(toStop))
            {
                admin.sendMessage(MsgSender.NPC_MANAGER, "path " + toStop + " is not active");
                return false;
            }
            BotManager.stopPath(toStop);

        }

        if (command.getName().equalsIgnoreCase("ptest") && args.length == 1)
        {
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "particle");
            npc.addTrait(new HitboxTest());
            npc.spawn(admin.getLocation());
        }
        return false;
    }
}
