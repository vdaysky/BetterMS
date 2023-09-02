package obfuscate.comand;

import obfuscate.game.player.StrikePlayer;
import obfuscate.message.MsgSender;
import obfuscate.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class WrappedSender {

    public CommandSender sender;

    public WrappedSender(CommandSender sender) {
        this.sender = sender;
    }

    public void sendMessage(MsgSender sender, String message)
    {
        this.sender.sendMessage(sender.form(message));
    }

    public StrikePlayer getPlayer() {
        if (sender instanceof Player)
            return StrikePlayer.getOrCreate(((Player) sender));
        return null;
    }

    public String getName() {
        if (sender instanceof Player playerSender) {
            return playerSender.getName();
        }
        return "Console";
    }

    public ConsoleCommandSender getConsoleSender() {
        if (sender instanceof ConsoleCommandSender)
            return (ConsoleCommandSender) sender;
        return null;
    }

    public boolean hasPermission(Permission requiredPermission) {

        // console sender has all permissions
        if (getPlayer() == null) {
            return true;
        }

        return getPlayer().hasPermission(requiredPermission);
    }
}
