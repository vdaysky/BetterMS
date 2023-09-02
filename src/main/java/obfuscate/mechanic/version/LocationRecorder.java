package obfuscate.mechanic.version;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.java.DefaultMap;
import obfuscate.util.java.Pair;

import java.util.ArrayList;
import java.util.UUID;

public class LocationRecorder {

    private static final DefaultMap<UUID, ArrayList<Pair<PlayerLocation, Long>>> locationHistory = new DefaultMap<>();

    public static void recordPlayerLocation(StrikePlayer player) {
        var locations = locationHistory.getOrDefault(player.getUuid(), new ArrayList<>());

        // prevent double logging the same tick
        if (!locations.isEmpty() && locations.get(locations.size() - 1).value() > System.currentTimeMillis() - 10) {
            MsdmPlugin.warn("Do not record player location, it was already recorded");
            return;
        }

        long ts = System.currentTimeMillis();
        locations.add(new Pair<>(new PlayerLocation(player.getLocation(), player.getEyeLocation()), ts));
//        MsdmPlugin.important("Add player location at " + ts + " for " + player.getName());
    }

    public static PlayerLocation getPlayerLocation(StrikePlayer player, int ticksBack) {
        long delta = ticksBack * 50L;
        long now = System.currentTimeMillis();
        long startTime = now - delta;

        var locations = locationHistory.getOrDefault(player.getUuid(), new ArrayList<>());

//        MsdmPlugin.important("There are " + locations.size() + " location for player " + player.getName());

        PlayerLocation loc = null;



        for (Pair<PlayerLocation, Long> location : locations) {
//            MsdmPlugin.important("Location time: " + location.value() + " expected one after " + startTime);
            if (location.value() > startTime) {
                MsdmPlugin.important("Found loc");
                loc = location.key();
                break;
            }
        }

        // delete all expired records
        // TODO: this may be laggy?
        for (UUID key : locationHistory.keySet()) {
            ArrayList<Pair<PlayerLocation, Long>> toDelete = new ArrayList<>();
            var locs = locationHistory.get(key);
            for (Pair<PlayerLocation, Long> x : locs) {
                if (x.value() < now - (20 * 50)) { // delete second old records
                    toDelete.add(x);
                }
            }
            locs.removeAll(toDelete);
        }
        return loc;
    }
}
