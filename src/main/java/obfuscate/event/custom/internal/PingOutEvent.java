package obfuscate.event.custom.internal;

import obfuscate.event.custom.ToBackendEvent;

import java.util.HashMap;

public class PingOutEvent implements ToBackendEvent {

    private final Integer pingID;

    public PingOutEvent(Integer pingID) {
        this.pingID = pingID;
    }

    @Override
    public String getName() {
        return "PingEvent";
    }

    @Override
    public HashMap<String, Object> getPayload() {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("ping_id", pingID);
        return payload;
    }
}
