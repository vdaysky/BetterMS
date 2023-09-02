package obfuscate.comand.team.leader;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.message.MsgSender;

public class TeamLeaderCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        
        if (ctx.getSender().getPlayer() == null) {
            ctx.getSender().sendMessage(MsgSender.CMD, "player is required for this command");
            return false;
        }

        if(ctx.getSender().getPlayer().getGame() == null) {
            ctx.getSender().sendMessage(MsgSender.CMD, "you have to be in-game.");
            return false;
        }

        return true;
    }
}
