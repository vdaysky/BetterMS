package obfuscate.team;

import org.bukkit.ChatColor;
import org.bukkit.Color;

public enum StrikeTeam
{
    T(ChatColor.RED, Color.fromRGB(255, 75, 75)),
    CT(ChatColor.AQUA, Color.fromRGB(125, 200, 255));

    final ChatColor _color;
    final Color _armorColor;

    StrikeTeam(ChatColor color, Color armorColor)
    {
        _color = color;
        _armorColor = armorColor;
    }

    public static StrikeTeam random()
    {
        return Math.random()>0.49? StrikeTeam.T: StrikeTeam.CT;
    }

    public String getColor()
    {
        return _color.toString();
    }

    public ChatColor getChatColor() {
        return _color;
    }

    public Color getArmorColor()
    {
        return _armorColor;
    }

    @Override
    public String toString() {
        return this == StrikeTeam.T?"Bombers":"SWAT";
    }

    public String getNiceName()
    {
        return toString();
    }

    public static StrikeTeam getOpposite(StrikeTeam team)
    {
        if (team == StrikeTeam.CT)
            return StrikeTeam.T;
        return StrikeTeam.CT;
    }

    public StrikeTeam getOpposite()
    {
        return getOpposite(this);
    }
}
