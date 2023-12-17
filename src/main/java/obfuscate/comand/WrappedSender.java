package obfuscate.comand;

import obfuscate.game.player.StrikePlayer;
import obfuscate.message.MsgSender;
import obfuscate.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class WrappedSender {

    public CommandSender sender;

    public WrappedSender(CommandSender sender) {
        this.sender = sender;
    }

    public void sendMessage(MsgSender sender, String message)
    {
        if (getPlayer() != null) {
            getPlayer().sendMessage(sender.form(message));
        }
        else {
            getConsoleSender().sendMessage(message);
        }
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

    public boolean hasPermission(Permission requiredPermission, ArrayList<Object> scopes) {

        // console sender has all permissions
        if (getPlayer() == null) {
            return true;
        }

        for (Object scope : scopes) {
            if (getPlayer().hasPermissionIn(requiredPermission, scope)) {
                return true;
            }
        }

        return getPlayer().hasPermission(requiredPermission);
    }

    @Override
    public int hashCode() {
        if (getPlayer() != null) {
            return getPlayer().hashCode();
        }
        return "Console".hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WrappedSender && obj.hashCode() == hashCode();
    }
}
