package obfuscate.network.models.responses;

import obfuscate.util.serialize.load.Loadable;

import java.util.HashMap;

public class IntentResponse extends EventResponse {

    @Loadable(field = "intent_success")
    Boolean success;

    @Loadable(field = "intent_message")
    String message;

    @Loadable(field = "payload")
    HashMap<String, Object> payload;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        /* Get user-friendly message describing intent outcome */
        return message;
    }

    public HashMap<String, Object> getPayload() {
        return payload;
    }

}
