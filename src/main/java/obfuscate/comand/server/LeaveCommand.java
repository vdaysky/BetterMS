package obfuscate.comand.server;

import obfuscate.MsdmPlugin;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.Game;
import obfuscate.message.MsgSender;
import org.bukkit.ChatColor;

public class LeaveCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {

        Game game = ctx.getSender().getPlayer().getGame();

        if (game == null) {
            ctx.getSender().sendMessage(MsgSender.SERVER, "You have to be in-game");
            return false;
        }

        game.tryLeavePlayer(ctx.getSender().getPlayer()).thenSync(intentResponse -> {
            MsdmPlugin.getGameServer().getFallbackServer().join(ctx.getSender().getPlayer());
            ctx.getSender().sendMessage(MsgSender.SERVER, ChatColor.GRAY + "You were moved to hub from game#" + game.getId().getObjId());
            return intentResponse;
        });

        return true;

    }
}
