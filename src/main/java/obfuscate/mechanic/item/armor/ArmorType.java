package obfuscate.mechanic.item.armor;

import obfuscate.mechanic.item.ItemStats;
import obfuscate.mechanic.item.guns.StrikeItemType;
import org.bukkit.Material;

public enum ArmorType implements ItemStats {
    HELMET("Helmet", 300, Material.LEATHER_HELMET, 39),
    KEVLAR("Kevlar", 650, Material.LEATHER_CHESTPLATE, 38);

    private String name;
    private Integer cost;
    private Material material;
    private int slot;

    ArmorType(String name, Integer cost, Material material, int slot)
    {
        this.name = name;
        this.cost = cost;
        this.material = material;
        this.slot = slot;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public StrikeItemType getItemType() {
        return StrikeItemType.ARMOR;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public Material getSkin() {
        return material;
    }



}
