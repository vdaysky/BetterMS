package obfuscate.podcrash;

import obfuscate.game.player.StrikePlayer;
import obfuscate.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BMSChannel implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String s, @NotNull Player player, @NotNull byte[] bytes) {

        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.get();
        int packetId = buf.getInt();

        if (packetId == 1) {
            Logger.info("Received BMS packet from " + player.getName() + ". Confirming Client use.", player);
            StrikePlayer sp = StrikePlayer.getOrCreate(player);
            sp.setBmsClientUsed(true);
            sp.updateTabName(null);
        }
    }
}
