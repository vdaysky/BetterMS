package obfuscate.ui.component;

import obfuscate.MsdmPlugin;
import obfuscate.event.bus.EventBus;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryButton extends Button<InventoryClickEvent> {

    private Integer subId;

    public InventoryButton(Material mat) {
        super(mat);
    }

    @Override
    void registerSelf(StrikePlayer holder, Integer slot) {
        subId = EventBus.addEventHandler(
            InventoryClickEvent.class,
            e -> {
                if (e.getSlot() == slot) {
                    onClick(e);
                    e.setCancelled(true);
                }
                return false;
            }
        );
    }

    @Override
    void deregisterSelf() {
        if (subId != null) {
            EventBus.unsubscribe(subId);
        }
    }
}
