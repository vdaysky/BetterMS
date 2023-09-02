package obfuscate.event.custom.internal;

import obfuscate.event.custom.ToBackendEvent;

import java.util.HashMap;

public class BukkitInitEvent implements ToBackendEvent {

    String secret;

    public BukkitInitEvent(String secret) {
        this.secret = secret;
    }

    @Override
    public String getName() {
        return "AuthorizeEvent";
    }

    @Override
    public HashMap<String, Object> getPayload() {
        var data = new HashMap<String, Object>();
        data.put("secret", secret);
        data.put("handle", "bukkit");
        return data;
    }
}
