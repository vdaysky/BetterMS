package obfuscate.comand.server;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.message.MsgSender;
import obfuscate.util.chat.C;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PrivateMessageCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        String name = ctx.getRequired("IGN");
        Player receiver = Bukkit.getPlayer(name);

        if (receiver == null) {
            ctx.getSender().sendMessage(MsgSender.SERVER, "Player " + name + " not found.");
            return false;
        }

        Bukkit.getConsoleSender().sendMessage(C.cDGreen + ctx.getSender().getPlayer().getName() + " -> to -> " + receiver.getName() + ": '" + ctx.getRequired("message") + "'");

        receiver.sendMessage(ctx.getSender().getPlayer().getName() + " > You: " + ctx.getRequired("message"));
        ctx.getSender().sendMessage(MsgSender.NONE, "You > " + receiver.getName() + ": " + ctx.getRequired("message"));
        return true;
    }
}
