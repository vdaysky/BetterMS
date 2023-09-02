package obfuscate.event.custom.shop;

import obfuscate.game.core.Game;

import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;
import org.bukkit.event.inventory.ClickType;

public class PlayerPreShopEvent extends ShopUpdateEvent
{
    private StrikePlayer player;
    private ClickType clickType;
    private StrikeItem clickedIcon;

    public PlayerPreShopEvent(Game game, StrikePlayer buyer, ClickType click, StrikeItem clickedIcon)
    {
        super(game, game.getShopManager().canShop(buyer));
        player = buyer;
        clickType = click;
        this.clickedIcon = clickedIcon;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public StrikeItem getClickedIcon() {
        return clickedIcon;
    }
}
