package obfuscate.util.alg;

import org.bukkit.Location;

import java.util.ArrayList;

public class TraceResult {

    private ArrayList<TraceEventV2> events;
    private Location lastLocation;

    public TraceResult(ArrayList<TraceEventV2> events, Location lastLocation) {
        this.events = events;
        this.lastLocation = lastLocation;
    }

    public ArrayList<TraceEventV2> getEvents() {
        return events;
    }

    public Location getLastLocation() {
        return lastLocation;
    }
}
