package obfuscate.mechanic.item;

import obfuscate.mechanic.item.guns.StrikeItemType;
import org.bukkit.Material;

public interface ItemStats
{
    int getCost();
    Material getSkin();
    String getName();
    StrikeItemType getItemType();
}
