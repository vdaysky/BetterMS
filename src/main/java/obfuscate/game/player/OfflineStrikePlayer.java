package obfuscate.game.player;

import obfuscate.event.CustomListener;
import obfuscate.network.models.schemas.PlayerData;
import org.bukkit.Bukkit;

public class OfflineStrikePlayer extends PlayerData implements CustomListener {

    public boolean isOnline() {
        return Bukkit.getPlayer(getUuid()) != null;
    }

}
