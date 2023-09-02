package obfuscate.mechanic.item;

import obfuscate.event.CustomListener;
import obfuscate.game.core.Game;
import obfuscate.game.core.GameSession;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.StrikeItemType;
import obfuscate.mechanic.item.utility.ConsumableItem;
import obfuscate.util.InvulnerableItem;
import obfuscate.util.UtilAction;
import obfuscate.util.UtilItem;
import obfuscate.util.chat.C;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class StrikeItem implements CustomListener, NamedDamageSource
{
	private final StrikeItemType _type;
	private final Integer _slot;
	private final ItemStats _stat;
	
	private String _ownerName;
	private ItemStack _stack;

	public StrikeItem(ItemStats stat)
	{
		_type = stat.getItemType();
		_stat = stat;
		_slot = stat.getItemType().getSlot();

		_stack = new ItemStack(getSkin(), 1);

		if (_stack.getDurability() > 0)
			_stack = UtilItem.makeUnbreakable(_stack);

		int hash = (int) (Math.random() * 100000000);
		ItemMeta meta = _stack.getItemMeta();
		meta.setDisplayName(ChatColor.BOLD + getName());
		meta.setLore(Collections.singletonList(C.cDGray + "id:" +  hash));
		_stack.setItemMeta(meta);
		fixStackName();
		
	}



	public StrikeItemType getType()
	{
		return _type;
	}

	public String getName()
	{
		return _stat.getName();
	}

	public int getCost()
	{
		return _stat.getCost();
	}

	public @Nullable Integer getSlot()
	{
		return _slot;
	}

	public Material getSkin()
	{
		return _stat.getSkin();
	}

	public String getOwnerName()
	{
		return _ownerName;
	}

	public void setOwnerName(String ownerName)
	{
		_ownerName = ownerName;
	}

	/**
	 * Animate drop and register dropped item in the game
	 * @param holder who dropped
	 * @param game game where drop happened
	 * @param natural drop as is without throw velocity
	 */
	public void drop(StrikePlayer holder, Game game, boolean natural)
	{

		_stack.setAmount(1);
		ItemStack uniqueCopy = new ItemStack(getSkin());

		ItemMeta meta = uniqueCopy.getItemMeta();
		meta.setDisplayName("unique" + Math.random());
		uniqueCopy.setItemMeta(meta);

		Item ent;

		if (natural)
		{
			ent = holder.getWorld().dropItemNaturally(holder.getEyeLocation(), uniqueCopy);
		}
		else
		{
			ent = holder.getWorld().dropItem(holder.getEyeLocation(), uniqueCopy);
			UtilAction.velocity(ent, holder.getLocation().getDirection(), 0.4, false, 0, 0.1, 1, false);
		}

		game.registerDroppedItem(ent, this);
		InvulnerableItem.register(ent);
	}

	public ItemStack getStack()
	{
		return _stack.clone();
	}

	public void giveToPlayer(Game game, StrikePlayer player, boolean setOwnerName)
	{
		GameSession session = game.getGameSession(player);

		if (setOwnerName)
			_ownerName = player.getName();

		fixStackName();
		if (this instanceof ConsumableItem)
		{
			if (getSlot() == null) {
				for (int i = 3; i < 7; i++) {
					if (!session.getInventory().hasItem(i) || session.getInventory().getItem(i).isAlike(this)) {
						session.getInventory().addItem(i, (ConsumableItem) this);
						return;
					}
				}
			} else {
				session.getInventory().addItem(getSlot(), (ConsumableItem) this);
			}
		}
		else
		{
			assert getSlot() != null;
			session.getInventory().setItem(getSlot(), this);
		}
	}

	public boolean isAlike(StrikeItem strikeItem) {
		return strikeItem.getSkin() == getSkin();
	}

	public boolean canPickup(Game game, StrikePlayer player)
	{
		GameSession session = game.getGameSession(player);

		if (session == null)
			return false;

		return !session.getInventory().hasItem(getSlot());
	}

	public void fixStackName()
	{
		ItemMeta meta = _stack.getItemMeta();
		meta.setDisplayName(C.cWhite + (_ownerName == null ? "" : _ownerName + "'s ") + C.Bold + getName());
		_stack.setItemMeta(meta);
	}


	public ItemStack getShopItem(int money, boolean alreadyHas, boolean canPickup) {
		return getShopItem(money, alreadyHas, canPickup, false);
	}

	public ItemStack getShopItem(int money, boolean alreadyHas, boolean canPickup, boolean isFree)
	{
		ArrayList<String> lore = new ArrayList<String>();

		ItemStack item = new ItemStack(getSkin());
		ItemMeta meta = item.getItemMeta();

//		for (String cur : _desc)
//			lore.add(C.cWhite + cur);

		//Custom (Gun Stats)
		if (getShopItemCustom().length > 0)
		{
			lore.add(" ");
			for (String cur : getShopItemCustom())
				lore.add(C.cWhite + cur);
		}

		if (alreadyHas)
		{
			lore.add(" ");
			lore.add(C.cYellow + C.Bold + "You already have this!");

			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}

		if (!alreadyHas || droppable() || canPickup)
		{
			if (!isFree) {
				lore.add(" ");
				lore.add(C.cYellow + C.Bold + "Cost: " + C.Reset + C.cDGreen + "$" + getCost());
				lore.add(" ");
				lore.add(C.cYellow + C.Bold + "Money: " + C.Reset + C.cDGreen + "$" + money);
			}

			lore.add(" ");
			lore.add((money >= getCost() || isFree) ? C.cGreen + C.Bold + "Click to Purchase" : C.cRed + C.Bold + "Not enough Money");
		}

		String[] loreArray = new String[lore.size()];
		loreArray = lore.toArray(loreArray);

		String name = getShopItemType() + " " + C.cGreen + getName();

		meta.setLore(Arrays.asList(loreArray));
		meta.setDisplayName(name);
		item.setItemMeta(meta);

//		if (alreadyHas)
//			UtilInv.addDullEnchantment(item);

		return item;
	}

	public String[] getShopItemCustom()
	{
		return new String[] {};
	}

	public String getShopItemType()
	{
		return "";
	}

	public ItemStats getStats(){return _stat;}

	public boolean droppable() {
		return false;
	}

	public abstract StrikeItem copy();

	public int getMaxStackSize() {
		// pretty much useless I think.
		// most certainly will define stack sizes in config
		// for now there are a bunch of overrides
		return _type.getMaxStackSize();
	}
}
	

