package obfuscate.event.custom.internal;

import obfuscate.event.custom.ToBackendEvent;

import java.util.HashMap;

public class ConfirmEvent implements ToBackendEvent {

    private final Integer messageID;
    private final HashMap<String, Object> payload;

    public ConfirmEvent(Integer messageID, HashMap<String, Object> payload) {
        this.messageID = messageID;
        this.payload = payload;
    }

    @Override
    public String getName() {
        return "ConfirmEvent";
    }

    @Override
    public HashMap<String, Object> getPayload() {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("confirm_message_id", messageID);
        payload.put("payload", this.payload);
        return payload;
    }
}
