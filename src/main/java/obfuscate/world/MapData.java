package obfuscate.world;

import obfuscate.team.StrikeTeam;
import obfuscate.util.Position;
import obfuscate.util.block.ComplexBoundingBox;

import java.util.ArrayList;

public interface MapData {
    String getName();

    boolean hasTag(String tag);

    ArrayList<Position> getDMRespawns();

    ArrayList<Position> getTeamRespawns(StrikeTeam team);

    ComplexBoundingBox getShop(StrikeTeam team);

    ComplexBoundingBox getASite();

    ComplexBoundingBox getBSite();
}
