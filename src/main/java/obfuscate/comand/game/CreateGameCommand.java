package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.event.custom.intent.CreateGameIntentEvent;
import obfuscate.message.MsgSender;
import obfuscate.network.models.responses.IntentResponse;
import obfuscate.util.Promise;
import org.bukkit.ChatColor;

public class CreateGameCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {

        String mapName = ctx.getLevel().getParsedRequired().getStr("map");
        String mode = ctx.getLevel().getParsedRequired().getStr("preset");

        ctx.getSender().sendMessage(MsgSender.PLUGIN, "Creating game...");

        Promise<? extends IntentResponse> response = new CreateGameIntentEvent(mapName, mode, ctx.getPlayer()).trigger();
        response.thenSync(
            intentResponse -> {
                if (intentResponse.isSuccess()) {
                    ctx.getSender().sendMessage(MsgSender.PLUGIN, ChatColor.GREEN + intentResponse.getMessage());
                } else {
                    ctx.getSender().sendMessage(MsgSender.PLUGIN, ChatColor.RED + intentResponse.getMessage());
                }
                return intentResponse;
            }
        );
        return true;
    }

    @Override
    public String wouldFail(ExecutionContext context) {
        return null;
    }
}
