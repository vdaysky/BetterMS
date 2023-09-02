package obfuscate.event.custom;

import java.util.HashMap;

/** Arbitrary event type containing name and payload
 * that can be sent to the backend.
 * Usages: sent to backend HTTP event endpoint or
 * through WebSocket connection.
*/
public interface ToBackendEvent {

    String getName();
    HashMap<String, Object> getPayload();
}
