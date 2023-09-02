package obfuscate.world;

import org.bukkit.World;

public class TempMap
{
    private final World _world;
    private final MapData _map;

    public TempMap(World world, MapData mapData)
    {
        _world = world;
        _map = mapData;
        world.setGameRuleValue("randomTickSpeed", "0");
    }

    public String getName()
    {
        return _map.getName();
    }

    public MapData getMapData() {
        return _map;
    }

    public World getWorld()
    {
        return _world;
    }

    public String getTempName()
    {
        return getWorld().getName();
    }

    public void unload()
    {
        MapManager.unloadMap(this);
    }

}
