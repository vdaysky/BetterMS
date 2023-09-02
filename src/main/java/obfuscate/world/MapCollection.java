package obfuscate.world;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapCollection
{
    private HashMap<String, GameMap> _maps;
    public MapCollection(@NotNull HashMap<String, GameMap> maps)
    {
        if (maps.isEmpty()) {
            throw new IllegalArgumentException("MapCollection cannot be empty");
        }

        _maps = maps;
    }

    public MapCollection getWithTags(String ... tags)
    {
        MapCollection init = new MapCollection(_maps);
        for (String tag : tags)
        {
            init = init.getWithTag(tag);
        }
        return init;
    }

    public MapCollection getWithTag(String tag)
    {
        HashMap<String, GameMap> withTag = new HashMap<>();
        for (String mapName : _maps.keySet())
        {
            GameMap map = _maps.get(mapName);
            if (map.hasTag(tag))
            {
                withTag.put(mapName, map);
            }
        }
        return new MapCollection(withTag);
    }

    public GameMap get(String mapName)
    {
        return _maps.get(mapName);
    }

    public GameMap pickRandom()
    {
        return new ArrayList<>(_maps.values()).get((int)(_maps.values().size() * Math.random()));
    }

    public boolean contains(GameMap map1)
    {
        for (GameMap map : _maps.values())
        {
            if (map.getName().equals(map1.getName()))
            {
                return true;
            }
        }
        return false;
    }

    public int size()
    {
        return _maps.values().size();
    }

    public List<GameMap> all() {
        return new ArrayList<>(_maps.values());
    }
}
