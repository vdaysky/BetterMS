package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.game.config.ConfigField;
import obfuscate.game.config.GameConfiguration;
import obfuscate.game.core.Game;
import obfuscate.message.MsgSender;
import obfuscate.util.chat.C;

public class VarCommand implements CommandExecutor {
    @Override
    public boolean execute(ExecutionContext ctx) {

        String varName = ctx.getRequired("key");
        Integer value = ctx.getOptional("value");

        ConfigField field = ConfigField.getField(varName);
        Game game = ctx.getPlayer().getGame();
        GameConfiguration config = game.getConfig();

        if (field == null)
        {
            ctx.getSender().sendMessage(MsgSender.CMD, C.cRed + "Failed to set '" + C.cYellow + varName + C.cRed + "': no such field");
            return false;
        }

        if (value == null)
        {
            ctx.getSender().sendMessage(MsgSender.CMD, C.cYellow + field.getName() + C.cGreen + ": " + field.getDescription());
            ctx.getSender().sendMessage(MsgSender.CMD, C.cGreen + "current: " + C.cYellow + config.getValue(field).val());
            ctx.getSender().sendMessage(MsgSender.CMD, C.cGreen + "default: " + C.cYellow + field.getDefaultValue());
            return false;
        }

        config.setValue(game, field, value);
        ctx.getSender().sendMessage(MsgSender.CMD, C.cGreen + "set '" + C.cYellow + varName + C.cGreen + "' to " + C.cYellow + value);
        return true;
    }

    @Override
    public String wouldFail(ExecutionContext context) {
        return null;
    }
}
