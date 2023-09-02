package obfuscate.mechanic.item.melee;

import obfuscate.mechanic.item.ItemStats;
import obfuscate.mechanic.item.guns.StrikeItemType;
import org.bukkit.Material;

public enum MeleeTypes implements ItemStats {

    KNIFE("Knife", 999999, Material.FEATHER);

    private String name;
    private Integer cost;
    private Material material;

    MeleeTypes(String name, Integer cost, Material material)
    {
        this.name = name;
        this.cost = cost;
        this.material = material;
    }


    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public Material getSkin() {
        return material;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public StrikeItemType getItemType() {
        return StrikeItemType.MELEE;
    }
}
