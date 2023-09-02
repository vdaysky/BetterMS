package obfuscate.comand.team.coach;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.Game;
import obfuscate.game.core.PlayerStatus;
import obfuscate.message.MsgSender;

public class StartCoachCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {

        Game game = ctx.getSender().getPlayer().getGame();
        if (ctx.getSender().getPlayer().getStatus(game) == PlayerStatus.COACHING)
        {
            ctx.getSender().sendMessage(MsgSender.GAME, "You are already coaching");
            return false;
        }

        ctx.getSender().sendMessage(MsgSender.GAME, "TODO: implement coach mode");
        ctx.getSender().sendMessage(MsgSender.GAME, "Now you are coaching");
        return true;
    }
}
