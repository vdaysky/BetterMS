package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.state.GameStateInstance;
import obfuscate.message.MsgSender;

public class SkipCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        GameStateInstance currentState = ctx.getSender().getPlayer().getGame().getGameState();

        ctx.getSender().sendMessage(MsgSender.PLUGIN, "Skipping current state: " + currentState.getName());
        ctx.getSender().getPlayer().getGame().setLeftDuration(0);
        return false;
    }
}
