package obfuscate.event.custom.backend.internal;

import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.network.annotations.Backend;

@Backend
public class AckConnEvent extends FromBackendEvent {

    public AckConnEvent(Object jsonEvt) {
        super(jsonEvt);
    }
}
