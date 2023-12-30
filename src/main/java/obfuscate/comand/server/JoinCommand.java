package obfuscate.comand.server;

import obfuscate.MsdmPlugin;
import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.Game;
import obfuscate.message.MsgSender;
import obfuscate.team.StrikeTeam;
import org.bukkit.ChatColor;

public class JoinCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {

        Integer id = ctx.getRequired("id");
        Boolean spec = ctx.getOptional("spec");
        String teamName = ctx.getOptional("team");

        StrikeTeam team = null;

        if (teamName != null) {
            team = StrikeTeam.valueOf(teamName);
        }

        if(spec == null) {
            spec = false;
        }

        Game game = MsdmPlugin.getGameServer().getGame(id);

        if (game == null) {
            ctx.getSender().sendMessage(MsgSender.SERVER, ChatColor.RED + "Lobby with id " + id + " does not exist!");
            return true;
        }

        else if (ctx.getSender().getPlayer().getGame() != null && ctx.getSender().getPlayer().getGame() == game && spec == ctx.getSender().getPlayer().isSpectating(ctx.getSender().getPlayer().getGame())) {
            ctx.getSender().sendMessage(MsgSender.SERVER, ChatColor.RED + "You are already in this lobby!");
            return true;
        }

        game.tryJoinPlayer(ctx.getSender().getPlayer(), spec, team);
        return true;
    }
}
