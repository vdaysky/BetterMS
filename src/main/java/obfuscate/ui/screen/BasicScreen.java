package obfuscate.ui.screen;

import obfuscate.event.bus.EventBus;
import obfuscate.game.player.StrikePlayer;
import obfuscate.ui.component.Button;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;

public class BasicScreen {

    private final ArrayList<Runnable> onCloseHandlers = new ArrayList<>();
    private Inventory handle;

    private String title = "Inventory";

    private boolean closeOnClick = false;

    private Integer size = 54;

    private final HashMap<Integer, Button<?>> buttons = new HashMap<>();

    public void show(StrikePlayer player) {

        handle = Bukkit.createInventory(null, size, title);

        for (Integer slot : buttons.keySet()) {
            var btn = buttons.get(slot);
            handle.setItem(slot, btn.getItemStack(player, slot));
            btn.setOnClick(e -> {
                if (closeOnClick) {
                    player.getPlayer().closeInventory();
                }
                return false;
            });
        }

        EventBus.addEventHandler(
                InventoryCloseEvent.class,
                e -> {
                    if (e.getInventory() == handle) {
                        closeInventory();
                        return true;
                    }
                    return false;
                }
        );
        player.getPlayer().openInventory(handle);
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setCloseOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
    }

    public void addButton(Integer slot, Button<?> button) {
        buttons.put(slot, button);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void closeInventory() {
        for (Button<?> b : buttons.values()) {
            b.deregister();
        }
    }
}
