package obfuscate.mechanic.item.objective;

import obfuscate.mechanic.item.ItemStats;
import obfuscate.mechanic.item.guns.StrikeItemType;
import org.bukkit.Material;

public enum MiscItemType implements ItemStats
{
    DEFUSER("Defuse kit", 300, Material.SHEARS),
    BOMB("C4", 0, Material.DIAMOND_SHOVEL),

    COMPASS("Tracking Compass", 0, Material.COMPASS),
    
    ;

    private String name;
    private Integer cost;
    private Material material;

    MiscItemType(String name, Integer cost, Material material)
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
        return StrikeItemType.EQUIPMENT;
    }
}
