package obfuscate.comand.team.coach;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.Game;
import obfuscate.message.MsgSender;

public class UncoachCommand implements CommandExecutor {


    @Override
    public boolean execute(ExecutionContext ctx) {
        Game game = ctx.getSender().getPlayer().getGame();

//        if (ctx.getSender().getPlayer().getState(game) != PlayerState.COACHING)
//        {
//            ctx.getSender().sendMessage(MsgSender.GAME, "You are not a coach");
//            return false;
//        }

        if (game.canJoin(ctx.getSender().getPlayer(), false))
        {
//            ctx.getSender().getPlayer().setState(game, PlayerState.PARTICIPATING);
            ctx.getSender().sendMessage(MsgSender.GAME, "Now you are participating");
            game.respawn(ctx.getSender().getPlayer(), false);
        }
        else
        {
            ctx.getSender().sendMessage(MsgSender.GAME, "You can't participate in this game");
            return false;
        }
        return true;
    }
}
