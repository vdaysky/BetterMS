package obfuscate.game.core;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.logging.Logger;
import obfuscate.mechanic.item.StrikeStack;
import obfuscate.mechanic.item.guns.StrikeItemType;
import obfuscate.mechanic.item.utility.ConsumableItem;
import obfuscate.mechanic.item.armor.ArmorType;
import obfuscate.mechanic.item.armor.StrikeArmor;
import obfuscate.mechanic.item.utility.grenade.Grenade;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.objective.DefusalKit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GameInventory implements Iterable<StrikeStack>
{
    private final StrikePlayer _holder;

    // grenades, bomb, defuse kit etc...
    private final StrikeStack[] hotbar = new StrikeStack[9];
    private StrikeStack[] armor = new StrikeStack[2];

    public GameInventory(StrikePlayer player)
    {
        _holder = player;

        for (int i = 0; i < 9; i++) {
            hotbar[i] = new StrikeStack(i, _holder);
        }

        armor[0] = new StrikeStack(ArmorType.KEVLAR.getSlot(), _holder);
        armor[1] = new StrikeStack(ArmorType.HELMET.getSlot(), _holder);
    }

    @Override
    public Iterator<StrikeStack> iterator() {
        Iterable<StrikeStack> iterable = Arrays.asList(hotbar);
        return iterable.iterator();
    }

    public boolean hasGuns() {
        for (StrikeStack stack : hotbar) {
            if (stack.isOf(Gun.class)) return true;
        }
        return false;
    }

    /** sync player's inventory when he joins */
    public void restore(Game game)
    {
        if (!_holder.isOnline()) {
            Logger.warning("Trying to restore offline inventory", game, _holder);
            return;
        }

        // clear vanilla inventory
        _holder.getPlayer().getInventory().clear();

        for (StrikeStack stack : hotbar)
        {
            if (!stack.isOf(Gun.class))
                continue;

            Gun gun = stack.top();
            gun.giveToPlayer(game, _holder,false);
        }

        _holder.equip(game);
    }

    public <X extends StrikeItem> @Nullable X getItem(int slot) {
        var stack = hotbar[slot];
        if (stack.isEmpty()) {
            return null;
        }
        return stack.top();
    }

    public Gun getGun(int slot)
    {
        if (hotbar[slot].isOf(Gun.class))
            return hotbar[slot].top();
        return null;
    }

    public void reloadGuns(IGame game)
    {
        for (StrikeStack stack : hotbar)
        {
            if (!stack.isOf(Gun.class))continue;
            ((Gun) stack.top()).refill();
        }
    }

    public @NotNull StrikeStack getStack(int absoluteSlot) {
        if (absoluteSlot < 9)
            return hotbar[absoluteSlot];
        return armor[39-absoluteSlot];
    }

    public void setStack(int absoluteSlot, StrikeStack stack, boolean update) {
        if (absoluteSlot < 9)
            hotbar[absoluteSlot].copyFrom(stack, update);
        else
            armor[39-absoluteSlot].copyFrom(stack, update);
    }

    public void setItem(int slot, StrikeItem item) {
        getStack(slot).set(item);
    }

    public boolean hasKit() {
        return getStack(8).isOf(DefusalKit.class);
    }

    public void clear()
    {
        for (int slot = 0; slot < 9; slot ++)
        {
            remove(slot);
        }
        armor[0].clear();
        armor[1].clear();
    }

    public void remove(int i) {
        getStack(i).clear();
    }

    public void setArmor(StrikeArmor strikeArmor)
    {
        int slot = 39 - strikeArmor.getSlot();
        armor[slot].set(strikeArmor);
    }

    public boolean hasHelmet()
    {
        int highestSlot = 39;
        StrikeStack helmetSlot = armor[highestSlot - ArmorType.HELMET.getSlot()];
        return !helmetSlot.isEmpty();
    }

    public boolean hasKevlar()
    {
        int highestSlot = 39;
        StrikeStack armorSlot = armor[highestSlot - ArmorType.KEVLAR.getSlot()];
        return !armorSlot.isEmpty();
    }

    public @Nullable StrikeStack findStack(Class<?extends StrikeItem> cls) {

        for (int i = 0; i < 9; i++){
            if (hotbar[i].isOf(cls)) {
                return hotbar[i];
            }
        }
        return null;
    }

    /**
     * Check if player has place for another (or new) item of that type
     * @param strikeItem item to check
     * @return true if another item cant fit into the slot or if item in slot cant be replaced by new item, false otherwise
     */
    public boolean alreadyHasMaxCapacity(@NotNull StrikeItem strikeItem) {

        StrikeStack stack;

        if (strikeItem.getSlot() == null) {

            stack = findStack(strikeItem.getClass());
            if (stack == null) {
                return false;
            }

        } else {
            stack = getStack(strikeItem.getSlot());
        }

        if (!stack.isEmpty()) {
            // if player has different item in slot
            if (!stack.top().getStats().equals(strikeItem.getStats())) {
                return false;
            }
        }

        return stack.getCount() >= strikeItem.getMaxStackSize();
    }

    public void addItem(int slot, ConsumableItem strikeItem)
    {
        var stack = hotbar[slot];

        if (stack.isEmpty()) {
            stack.set(strikeItem);
        }
        else if (stack.isOf(strikeItem.getType())) {
            stack.add(strikeItem);
        }
        else {
            System.out.println("[WARN] tried to stack not stackable items");
        }
    }

    public boolean hasItem(int slot) {
        return !getStack(slot).isEmpty();
    }

    public ArrayList<Grenade> getGrenades()
    {
        ArrayList<Grenade> grenades = new ArrayList<>();
        for (StrikeStack stack : hotbar)
        {
            if (stack.isOf(StrikeItemType.GRENADE))
                grenades.addAll(stack.getItems());
        }
        return grenades;
    }

    public void refreshHotBarDisplay() {
        for (StrikeStack stack : hotbar) {
            stack.refreshSlotDisplay();
        }
    }

    public List<Gun> getGuns() {
        return Arrays.stream(hotbar)
                .filter(x -> !x.isEmpty() && x.top() instanceof Gun)
                .map(x -> (Gun) x.top())
                .toList();
    }
}
