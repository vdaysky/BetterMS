package obfuscate.comand.game;

import obfuscate.comand.ExecutionContext;
import obfuscate.comand.builder.CommandExecutor;
import obfuscate.message.MsgSender;
import obfuscate.util.UtilMath;

public class ForceStartCommand implements CommandExecutor {

    @Override
    public boolean execute(ExecutionContext ctx) {

        String delay = ctx.getOptional("delay");

        char unit = 's';
        String duration = "0";

        if (delay != null) {
            unit = delay.charAt(delay.length() - 1);
            duration = delay.substring(0, delay.length()-1);
        }

        if (UtilMath.isNumeric(duration)) {
            int parsed_delay = Integer.parseInt(duration);
            if (unit == 's') {
                ctx.getSender().getPlayer().getGame().forceStart(parsed_delay);
            }
            else if (unit == 'm') {
                ctx.getSender().getPlayer().getGame().forceStart(parsed_delay * 60);
            }
            else {
                ctx.getSender().sendMessage(MsgSender.GAME, "'" + unit + "'" + " is not a supported unit. Supported units are: s, m");
            }
        }
        else {
            ctx.getSender().sendMessage(MsgSender.GAME, "delay has to be numeric");
        }
        return false;
    }
}
