package obfuscate.ui.component;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public abstract class Button<ClickEvent extends Cancellable> {

    private final Material material;
    private String title;
    private String[] lore;
    private Function<ClickEvent, Boolean> onClick;

    private ItemStack stack = null;
    public Button(Material mat) {
        material = mat;
    }

    abstract void registerSelf(StrikePlayer holder, Integer item);
    abstract void deregisterSelf();

    public Button<ClickEvent> setTitle(String title) {
        this.title = title;
        return this;
    }

    public Button<ClickEvent> setLore(String... lore) {
        this.lore = lore;
        return this;
    }

    public Button<ClickEvent> setOnClick(Function<ClickEvent, Boolean> onClick) {
        if (this.onClick == null) {
            this.onClick = onClick;
        } else {
            final var oldHandler = this.onClick;
            this.onClick = e -> {
                oldHandler.apply(e);
                onClick.apply(e);
                return null;
            };
        }
        return this;
    }

    public void deregister() {
        if (this.stack == null) {
            return;
        }
        deregisterSelf();
    }

    public ItemStack getItemStack(StrikePlayer holder, Integer slot) {
        if (stack != null) {
            return stack;
        }
        ItemStack item = new ItemStack(material);
        var meta = item.getItemMeta();

        if (title != null) {
            meta.setDisplayName(title);
        }

        if (lore != null) {
            meta.setLore(java.util.Arrays.asList(lore));
        }

        item.setItemMeta(meta);

        stack = item;
        registerSelf(holder, slot);

        return item;
    }

    protected void onClick(ClickEvent event) {
        if (onClick == null)
            return;

        MsdmPlugin.highlight("OnClick");
        Boolean cancelled = onClick.apply(event);

        if (cancelled == null) {
            return;
        }

        if (cancelled) {
            event.setCancelled(true);
        }
    }
}
