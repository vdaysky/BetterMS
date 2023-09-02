package obfuscate.world;

import obfuscate.team.StrikeTeam;
import obfuscate.util.Position;
import obfuscate.util.block.ComplexBoundingBox;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GameMap implements MapData
{
    private final String _mapName;
    private final ArrayList<Position>  _teamT_Respawns;
    private final ArrayList<Position>  _teamCT_Respawns;
    private final ArrayList<Position>  _randomRespawns;
    private final List<String> _tags;
    private final ComplexBoundingBox tShop;
    private final ComplexBoundingBox ctShop;
    private final ComplexBoundingBox ASite;
    private final ComplexBoundingBox BSite;

    public GameMap(
            String mapName,
            ArrayList<Position> teamTRespawns,
            ArrayList<Position> teamCTRespawns,
            ArrayList<Position> randomRespawns,
            ComplexBoundingBox tShop,
            ComplexBoundingBox ctShop,
            ComplexBoundingBox ASite,
            ComplexBoundingBox BSite,
            @Nullable List<String> tags
    )
    {
        _mapName = mapName;
        _teamT_Respawns = teamTRespawns;
        _teamCT_Respawns = teamCTRespawns;
        _randomRespawns = randomRespawns;

        if (tags == null) {
            tags = new ArrayList<>();
        }

        _tags = tags;

        this.tShop = tShop;
        this.ctShop = ctShop;
        this.ASite = ASite;
        this.BSite = BSite;
    }

    public void refresh() {
        GameMap newMap = MapManager.loadMapData(this._mapName);

        if (newMap == null) {
            return;
        }

        this._teamT_Respawns.clear();
        this._teamCT_Respawns.clear();
        this._randomRespawns.clear();
        this._teamT_Respawns.addAll(newMap._teamT_Respawns);
        this._teamCT_Respawns.addAll(newMap._teamCT_Respawns);
        this._randomRespawns.addAll(newMap._randomRespawns);
        this._tags.clear();
        this._tags.addAll(newMap._tags);
    }

    public String getName()
    {
        return _mapName;
    }

    public boolean hasTag(String tag)
    {
        return _tags != null && _tags.contains(tag);
    }


    public ArrayList<Position> getDMRespawns()
    {
        return _randomRespawns;
    }

    public ArrayList<Position> getTeamRespawns(StrikeTeam team)
    {
        if (team == StrikeTeam.T)
            return _teamT_Respawns;
        return _teamCT_Respawns;
    }

    public boolean isShop(Location location, StrikeTeam team)
    {
        if (team == StrikeTeam.T)
            return tShop.isInside(location);
        else if (team == StrikeTeam.CT)
            return ctShop.isInside(location);

        return tShop.isInside(location) || ctShop.isInside(location);
    }

    public ComplexBoundingBox getASite()
    {
        return ASite;
    }

    public ComplexBoundingBox getBSite()
    {
        return BSite;
    }

    public ComplexBoundingBox getShop(StrikeTeam team)
    {
        if (team == StrikeTeam.T)
            return tShop;
        return ctShop;
    }

    public boolean isSite(Location loc)
    {
        return (ASite != null && ASite.isInside(loc)) || (BSite != null && BSite.isInside(loc));
    }

    public @Nullable String getSiteName(Location loc)
    {
        if (ASite != null && ASite.isInside(loc))
            return "A";
        if (BSite != null && BSite.isInside(loc))
            return "B";
        return null;
    }

    public HashMap<String, Object> toObject() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("T Respawns", _teamT_Respawns.stream().map( pos -> Arrays.asList(pos.getX(), pos.getY(), pos.getZ(), pos.getPitch(), pos.getYaw())).toList());
        map.put("CT Respawns", _teamCT_Respawns.stream().map( pos -> Arrays.asList(pos.getX(), pos.getY(), pos.getZ(), pos.getPitch(), pos.getYaw())).toList());
        map.put("DM Respawns", _randomRespawns.stream().map( pos -> Arrays.asList(pos.getX(), pos.getY(), pos.getZ(), pos.getPitch(), pos.getYaw())).toList());
        map.put("T Shop", tShop.toObject());
        map.put("CT Shop", ctShop.toObject());
        map.put("Bomb Site A", ASite.toObject());
        map.put("Bomb Site B", BSite.toObject());
        map.put("Tags", _tags);

        return map;
    }
}
