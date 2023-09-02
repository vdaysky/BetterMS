package obfuscate.event.custom.backend.internal;

import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.network.annotations.Backend;
import obfuscate.util.serialize.load.Loadable;

@Backend
public class PingInEvent extends FromBackendEvent {

    @Loadable(field = "ping_id")
    public Integer pingId;

    public PingInEvent(Object jsonEvt) {
        super(jsonEvt);
    }
}
