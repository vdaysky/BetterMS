package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.message.MsgSender;
import obfuscate.util.time.Task;

public class RestartCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {
        int delay = 5;

        Integer arg_delay = ctx.getOptional("delay");

        if (arg_delay != null)
            delay = arg_delay;

        ctx.getSender().getPlayer().getGame().broadcastChat(MsgSender.GAME, "Game will be restarted in " + delay + " seconds!");
        new Task(ctx.getSender().getPlayer().getGame()::restart, delay * 20).run();
        return false;
    }
}
