package obfuscate.util.sidebar;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.registry.RegistryModule;


import java.util.UUID;

public class UniqueSidebar
{
    private static final RegistryModule<Sidebar> sidebarReg = MsdmPlugin.getGameServer().getOnlinePlayerDataRegistry().getModule("UniqueSidebar");

    public static Sidebar getOrCreate(StrikePlayer player)
    {
        UUID key = player.getUuid();
        if (!sidebarReg.hasEntry(key)) {
            Sidebar s = new Sidebar(player);
            sidebarReg.addEntry(key, s);
            s.create();
        }
        return sidebarReg.getEntry(key);
    }

    public static void deregisterBoard(StrikePlayer player) {
        UUID key = player.getUuid();

        if (sidebarReg.hasEntry(key)) {
            sidebarReg.removeEntry(key).destroy();
        }
    }
}
