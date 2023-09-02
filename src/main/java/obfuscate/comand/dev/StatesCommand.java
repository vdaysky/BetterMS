package obfuscate.comand.dev;

import obfuscate.MsdmPlugin;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.GameStateInstance;
import obfuscate.message.MsgSender;
import org.bukkit.ChatColor;

public class StatesCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        for (Game game : MsdmPlugin.getGameServer().getGames())
        {
            ctx.getSender().sendMessage(MsgSender.NONE, "");
            ctx.getSender().sendMessage(MsgSender.NONE, "");
            ctx.getSender().sendMessage(MsgSender.PLUGIN, "============ Game: " + game.getId().getObjId() + " ============");
            ctx.getSender().sendMessage(MsgSender.PLUGIN, ChatColor.BOLD + "In progress: " + ChatColor.RESET + game.isInProgress());
            GameStateInstance state = game.getGameState();
            ctx.getSender().sendMessage(MsgSender.PLUGIN, ChatColor.BOLD + "State: " + ChatColor.RESET + state.getName() + " " + state.getTags());
            ctx.getSender().sendMessage(MsgSender.PLUGIN, ChatColor.BOLD + "> Players:");

            for (StrikePlayer p : game.getEverParticipated()) {
                if (p.isOnline()) {
                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "- " + p.getName() +
                            " has state " + game.getGameSession(p).getState() +
                            ", status: " + game.getGameSession(p).getStatus());
                } else {
                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "- OfflinePlayer(" + p.getId().getObjId() + ")" +
                            " has state " + game.getGameSession(p).getState() +
                            ", status: " + game.getGameSession(p).getStatus());
                }
                ctx.getSender().sendMessage(MsgSender.PLUGIN, "  alive: " + game.getGameSession(p).isAlive());

                // offline player will be deleted in some game modes from the in-game team
                var roster = game.getPlayerRoster(p);
                if (roster != null)
                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "  roster: " +roster.getName());
                else
                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "  roster: null");
            }

            ctx.getSender().sendMessage(MsgSender.PLUGIN, "==============================================");
        }
        return true;
    }
}
