package obfuscate.event.custom.server;

import obfuscate.event.Tracked;
import obfuscate.event.custom.CustomEvent;

@Tracked
public class ServerStartEvent extends CustomEvent {

    Integer serverId;

    public ServerStartEvent(Integer serverId) {
        this.serverId = serverId;
    }

    public Integer getServerId() {
        return serverId;
    }

}
