package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.core.Game;
import obfuscate.message.MsgSender;

public class ReadyCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        Game game = ctx.getSender().getPlayer().getGame();
        if (game == null)
        {
            ctx.getSender().sendMessage(MsgSender.SERVER, "You are not in game!");
            return false;
        }

        if (!game.readyStateRequiredByConfig()) {
            ctx.getSender().sendMessage(MsgSender.SERVER, "Game does not require you to be ready! Rude ikr");
            return false;
        }

        if (game.isPlayerReady(ctx.getSender().getPlayer())) {
            ctx.getSender().sendMessage(MsgSender.SERVER, "You are already !ready");
            return false;
        }

        game.setPlayerReady(ctx.getSender().getPlayer());
        return true;
    }
}
