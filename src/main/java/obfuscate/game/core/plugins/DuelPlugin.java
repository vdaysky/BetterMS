package obfuscate.game.core.plugins;

import obfuscate.game.config.ConfigField;
import obfuscate.gamemode.Competitive;

public class DuelPlugin implements IPlugin<Competitive>{
    @Override
    public void preInit(Competitive instance) {
        instance.getConfig().setValue(instance, ConfigField.BUY_TIME, -1);
        instance.getConfig().setValue(instance, ConfigField.BUY_ANYWHERE, 1);
        instance.getConfig().setValue(instance, ConfigField.FREE_GUNS, 1);
        instance.getConfig().setValue(instance, ConfigField.MAX_PLAYERS, 2);
        instance.getConfig().setValue(instance, ConfigField.KEEP_INVENTORY, 1);
        instance.getConfig().setValue(instance, ConfigField.FREEZE_TIME_DURATION, 5);
        instance.getConfig().setValue(instance, ConfigField.ROUND_END_DURATION, 3);
        instance.getConfig().setValue(instance, ConfigField.USE_ECONOMY, 0);
    }
}
