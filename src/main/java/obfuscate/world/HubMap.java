package obfuscate.world;

import obfuscate.team.StrikeTeam;
import obfuscate.util.Position;
import obfuscate.util.block.ComplexBoundingBox;

import java.util.ArrayList;

public class HubMap implements MapData {

    private String name;

    public HubMap(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasTag(String tag) {
        return false;
    }

    @Override
    public ArrayList<Position> getDMRespawns() {
        return null;
    }

    @Override
    public ArrayList<Position> getTeamRespawns(StrikeTeam team) {
        return null;
    }

    @Override
    public ComplexBoundingBox getShop(StrikeTeam team) {
        return null;
    }

    @Override
    public ComplexBoundingBox getASite() {
        return null;
    }

    @Override
    public ComplexBoundingBox getBSite() {
        return null;
    }
}
