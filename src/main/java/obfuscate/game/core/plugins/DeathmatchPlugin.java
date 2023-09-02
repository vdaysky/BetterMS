package obfuscate.game.core.plugins;

import obfuscate.event.LocalEvent;
import obfuscate.event.custom.damage.PlayerDeathEvent;
import obfuscate.event.custom.player.PlayerJoinGameEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.config.GameConfiguration;
import obfuscate.game.core.traits.DeathmatchStateUpdater;
import obfuscate.game.core.traits.ISharedContext;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.guns.GunStats;
import obfuscate.message.MsgSender;
import obfuscate.util.chat.C;

public class DeathmatchPlugin implements IPlugin<Competitive> {

    @Override
    public void preInit(Competitive instance) {

        GameConfiguration config = instance.getConfig();

        // configure some DM specific shit
        config.setValue(instance, ConfigField.INFINITE_AMMO, 2);
        config.setValue(instance, ConfigField.MAX_GRENADES, 0);
        config.setValue(instance, ConfigField.BUY_ANYWHERE, 1);
        config.setValue(instance, ConfigField.BUY_TIME, -1);
        config.setValue(instance, ConfigField.FREE_GUNS, 1);
        config.setValue(instance, ConfigField.LIVE_DURATION, 60 * 30);
        config.setValue(instance, ConfigField.ALLOW_TEAM_DAMAGE, 1);
        config.setValue(instance, ConfigField.WINS_BY_ELIMINATION, 0);
        config.setValue(instance, ConfigField.RANDOM_SPAWNS, 1);
        config.setValue(instance, ConfigField.KEEP_INVENTORY, 1);
        config.setValue(instance, ConfigField.CAN_SEE_TEAM_NAMES, 0);
        config.setValue(instance, ConfigField.DINK_ON_KILL, 1);
        config.setValue(instance, ConfigField.ITEM_DESTROY_ON_DROP, 1);
        config.setValue(instance, ConfigField.MIN_PLAYERS, 2);
        config.setValue(instance, ConfigField.ALLOW_ITEM_DROP, 0);

        ISharedContext traits = instance.getSharedContext();

        traits.setGameStateUpdater(new DeathmatchStateUpdater());
    }

    @LocalEvent
    public void healHPOnKill(PlayerDeathEvent e)
    {
        if (e.getDamager() == null)
            return;

        if (e.getModifiers().isHeadshot())
        {
            e.getDamager().partialHeal(0.5f, e.getGame());
            e.getDamager().sendMessage(MsgSender.DEATHMATCH, C.cGray + "Healed " + C.cYellow  + "50% HP" + C.cGray  + " for a headshot");
        }
        else
        {
            e.getDamager().partialHeal(0.2f, e.getGame());
            e.getDamager().sendMessage(MsgSender.DEATHMATCH, C.cGray  + "Healed " + C.cYellow + "20% HP" + C.cGray  + " for a kill");
        }

        e.getDamager().reloadAll(e.getGame());
    }

    @LocalEvent
    private void setPlayerGuns(PlayerDeathEvent e)
    {
        e.getDamagee().reloadAll(e.getGame());
    }

    @LocalEvent
    private void setInventory(PlayerJoinGameEvent e)
    {
        StrikePlayer player = e.getPlayer();

        if (!e.isSpectator())
        {
            e.getGame().getShopManager().updateInventoryShop(player, true);

            new Gun(GunStats.AK47).giveToPlayer(e.getGame(), player, true);
            new Gun(GunStats.DEAGLE).giveToPlayer(e.getGame(), player, true);
        }
    }
}
