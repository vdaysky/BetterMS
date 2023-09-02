package obfuscate.gamemode.registry;
import obfuscate.util.chat.C;
import org.bukkit.Material;

import java.util.ArrayList;

/** Stores game types that are available publicly in menu */
public enum GameMode
{
    COMPETITIVE(
            Material.DIAMOND_SHOVEL,
            "Defusal",
            new String[]{
                C.cGold + "Classic 5v5 competitive defusal mode."
            }
    ),

    PUB(
            Material.DIAMOND_SHOVEL,
            "Pub",
            new String[]{
                    C.cGold + "Casual 5v5 defusal mode."
            }
    ),

    DEATHMATCH(
        Material.TNT,
        "FFA Deathmatch",
        new String[]{
                C.cGold + "Free for all deathmatch mode. ",
                C.cGold + "Any guns, instant respawn, no objectives.",
                C.cGold + "Get as many kills as you can!"
        }
    ),

    DUEL(
        Material.FEATHER,
        "1v1",
        new String[]{
                C.cGold + "1v1 mode."
        }
    ),

    PRACTICE(
        Material.FEATHER,
        "Practice",
        new String[]{
                C.cGold + "Practice mode."
        }
    ),

    RANKED(
        Material.SUNFLOWER,
        "Ranked",
        new String[]{
                C.cGold + "Ranked defusal game."
        }
    ),
    GUNGAME(
        Material.WOODEN_SHOVEL, // AK
        "GunGame",
        new String[]{
                C.cGold + "19 Levels, get kills with each weapon",
                C.cGold + "to progress to the next level. First to",
                C.cGold + "get a kill with each weapon wins!"
        }
    )
    ;

    final Material icon;
    final String gameName;

    final String[] description;

    GameMode(Material icon, String gameName, String[] description)
    {
        this.icon = icon;
        this.gameName = gameName;
        this.description = description;
    }

    public static ArrayList<String> getGameModeNames()
    {
        ArrayList<String> names = new ArrayList<>();
        for(GameMode mode : GameMode.values())
        {
            names.add(mode.name());
        }
        return names;
    }

    public String getGameName(){
        return gameName;
    }

    public Material getIcon() {
        return icon;
    }

    public String[] getDescription() {
        return description;
    }
}
