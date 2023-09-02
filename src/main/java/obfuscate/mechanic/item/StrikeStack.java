package obfuscate.mechanic.item;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.armor.ArmorType;
import obfuscate.mechanic.item.guns.StrikeItemType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A slot of Player's inventory in a Game
 */
public class StrikeStack {

    ArrayList<StrikeItem> items = new ArrayList<>();

    StrikePlayer holder;
    int realSlot;

    public StrikeStack(int slot, StrikePlayer holder) {
        this.realSlot = slot;
        this.holder = holder;
    }

    public void refreshSlotDisplay() {
        if (this.getRealSlot() > 9) {
            // don't touch other armor
            if (this.getRealSlot() != ArmorType.HELMET.getSlot()) {
                return;
            }
        }

        if (holder.getPlayer() == null) {
            return;
        }

        var invHandle = holder.getPlayer().getInventory();

        if (this.isEmpty()) {
            invHandle.setItem(this.getRealSlot(), null);
            return;
        }

        var stackHandle = this.top().getStack();
        stackHandle.setAmount(this.getCount());
        // somehow this line causes PlayerInteractEvent to be called twice, which
        // in turn will cause double grenade throw. I don't know why, I don't want
        // to know why
        invHandle.setItem(this.getRealSlot(), stackHandle);
    }

    public int getCount() {
        return this.items.size();
    }

    public boolean isEmpty() {
        return this.getCount() == 0;
    }

    public <X extends StrikeItem> X top() {
        return (X) this.items.get(0);
    }

    public <X extends StrikeItem> X pop() {
        X res = (X) this.items.remove(0);
        this.refreshSlotDisplay();
        return res;
    }

    public boolean isOf(Class<? extends StrikeItem> cls) {
        if (isEmpty()) {
            return false;
        }
        return items.get(0).getClass().isAssignableFrom(cls);
    }

    public boolean isOf(StrikeItemType itemType) {
        if (isEmpty()) {
            return false;
        }

        return top().getType().equals(itemType);
    }

    public void add(StrikeItem item) {
        this.items.add(item);
        this.refreshSlotDisplay();
    }

    public void set(@Nullable StrikeItem item) {
        this.items.clear();
        if (item != null) {
            this.items.add(item);
        }
        this.refreshSlotDisplay();
    }

    public <X extends StrikeItem> List<X> getItems() {
        ArrayList<X> castItems = new ArrayList<>();
        for (StrikeItem item : items) {
            castItems.add((X) item);
        }
        return castItems;
    }

    public int getRealSlot() {
        return realSlot;
    }

    public void clear(boolean update) {
        this.items.clear();
        if (update) {
            this.refreshSlotDisplay();
        }
    }

    public void clear() {
        this.clear(true);
    }

    public StrikeStack copyFrom(StrikeStack stack, boolean update) {
        this.items.clear();
        this.items.addAll(stack.items);
        if (update) {
            this.refreshSlotDisplay();
        }
        return this;
    }

    public StrikeStack copy() {
        return new StrikeStack(this.getRealSlot(), this.holder).copyFrom(this, false);
    }

    public void giveToPlayer(Game game, StrikePlayer player, boolean setOwnerName) {
        for (StrikeItem item : this.items) {
            item.giveToPlayer(game, player, setOwnerName);
        }
    }
}
