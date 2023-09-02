package obfuscate.ui.component;

import obfuscate.event.bus.EventBus;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

public class HotbarButton extends Button<PlayerInteractEvent> {

    private Integer subId;

    public HotbarButton(Material mat) {
        super(mat);
    }

    @Override
    void registerSelf(StrikePlayer holder, Integer slot) {
        subId = EventBus.addEventHandler(
            PlayerInteractEvent.class,
            e -> {
                if (holder.getPlayer() != e.getPlayer()) {
                    return null;
                }

                if (e.getPlayer().getInventory().getHeldItemSlot() == slot) {
                    onClick(e);
                }

                return null;
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
