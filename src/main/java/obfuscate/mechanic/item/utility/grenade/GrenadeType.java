package obfuscate.mechanic.item.utility.grenade;

import obfuscate.mechanic.item.ItemStats;
import obfuscate.mechanic.item.guns.StrikeItemType;
import org.bukkit.Material;

public enum GrenadeType implements ItemStats
{
    HE("HE Grenade", 300, Material.APPLE, 1, null),
    FLASH("Flashbang", 200, Material.CARROT, 2, null),
    SMOKE("Smoke", 300, Material.POTATO, 1, null),
    MOLOTOV("Molotov", 400, Material.COOKED_PORKCHOP, 1, null),
    INCENDIARY("Incendiary", 600, Material.PORKCHOP, 1, null);

    private String name;
    private int cost;
    private Material skin;
    private int limit;
    private Integer slot;

    GrenadeType(String name, int cost, Material skin, int limit, Integer slot)
    {
        this.name = name;
        this.cost = cost;
        this.skin = skin;
        this.limit = limit;
        this.slot = slot;
    }

    public int getCost()
    {
        return cost;
    }

    public int getLimit() {
        return limit;
    }

    public Material getSkin() {
        return skin;
    }

    public String getName() {
        return name;
    }

    @Override
    public StrikeItemType getItemType() {
        return StrikeItemType.GRENADE;
    }

    public Integer getSlot()
    {
        return slot;
    }
}
