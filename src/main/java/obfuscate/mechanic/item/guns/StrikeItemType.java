package obfuscate.mechanic.item.guns;

import javax.annotation.Nullable;

public enum StrikeItemType
{
	PRIMARY_WEAPON(0, 1),
	SECONDARY_WEAPON(1, 1),
	MELEE(2, 1),
	GRENADE(-1, -1),
	EQUIPMENT(8, 1),
	ARMOR(-1, 1);

	Integer slot;
	int maxStackSize;

	StrikeItemType(Integer slot, int maxStackSize)
	{
		this.slot = slot;
		this.maxStackSize = maxStackSize;
	}
	public @Nullable Integer getSlot()
	{
		return slot;
	}

	public int getMaxStackSize() {
		return maxStackSize;
	}
}
