package obfuscate.event.custom.item;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;
import org.bukkit.entity.Item;

public class ItemPickUpEvent extends CancellableEvent
{
    private Item entity;
    private StrikeItem item;
    private Game Game;
    private StrikePlayer player;

    public ItemPickUpEvent(Item entity, StrikeItem item, Game Game, StrikePlayer player) {
        this.entity = entity;
        this.item = item;
        this.Game = Game;
        this.player = player;
    }

    public Item getEntity() {
        return entity;
    }

    public StrikeItem getItem() {
        return item;
    }

    public Game getGame() {
        return Game;
    }

    public StrikePlayer getPlayer() {
        return player;
    }
}
