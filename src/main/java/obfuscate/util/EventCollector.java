package obfuscate.util;

import obfuscate.game.player.StrikePlayer;
import obfuscate.util.java.DefaultMap;

import java.util.ArrayList;
import java.util.UUID;

public class EventCollector {

    static DefaultMap<UUID, DefaultMap<String, ArrayList<Long>>> events = new DefaultMap<>();

    public static void record(StrikePlayer player, String entry) {
        var playerEvents = events.getOrDefault(player.getUuid(), new DefaultMap<>());
        long ts = System.currentTimeMillis();
        playerEvents.getOrDefault(entry, new ArrayList<>()).add(ts);
    }

    public static int count(StrikePlayer player, String entry, int ticks) {
        var eventsOfPlayer = events.get(player.getUuid());
        if (eventsOfPlayer == null) {
            return 0;
        }
        var timestamps = eventsOfPlayer.get(entry);

        if (timestamps == null) {
            return 0;
        }

        int count = 0;

        long now = System.currentTimeMillis();

        ArrayList<Long> toRemove = new ArrayList<>();
        for (Long ts : timestamps) {
            long start = now - (ticks * 50L);
            if (ts >= start) {
                count++;
            } else {
                toRemove.add(ts);
            }
        }
        timestamps.removeAll(toRemove);
        return count;
    }

}
