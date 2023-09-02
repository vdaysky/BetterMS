package obfuscate.game.core;

import obfuscate.event.LocalEvent;
import obfuscate.event.custom.shop.PlayerPreShopEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.team.StrikeTeam;
import obfuscate.game.shop.ShopManager;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.message.MsgSender;
import obfuscate.util.chat.C;
import org.bukkit.Location;
import org.bukkit.event.inventory.ClickType;

public abstract class ShoppableGame extends Game {

    public abstract ShopManager getShopManager();

    public boolean isLocationShoppable(Location location, StrikeTeam team)
    {
        return getGameMap().isShop(location, team);
    }

    @LocalEvent
    private void updateInventoryShop(TimeEvent e)
    {
        if (e.getReason() != TimeEvent.UpdateReason.SECOND)
            return;

        for (StrikePlayer player : this.getOnlinePlayers())
        {
            boolean canShop = getShopManager().canShop(player);
            getShopManager().updateInventoryShop(player, canShop);
        }
    }

    @LocalEvent
    private void playerShopEvent(PlayerPreShopEvent event)
    {
        boolean drop = event.getClickType() == ClickType.DROP;
        StrikePlayer player = event.getPlayer();
        StrikeItem item = event.getClickedIcon();

        if (!getShopManager().canAfford(player, item.getStats().getCost())) {
            player.sendMessage(MsgSender.GAME, C.cRed + "You don't have enough money!");
            event.getGame().getSoundManager().bass().forPlayer(player).play();
            return;
        }

        getShopManager().buy(player, item, drop);
        getShopManager().updateInventoryShop(player, true);
    }

}
