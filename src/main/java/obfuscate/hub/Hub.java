package obfuscate.hub;

import obfuscate.event.CustomListener;
import obfuscate.event.LocalEvent;
import obfuscate.event.custom.game.PlayerChatEvent;
import obfuscate.event.custom.lobby.PlayerJoinHubEvent;
import obfuscate.event.custom.lobby.PlayerLeaveHubEvent;
import obfuscate.event.custom.network.ModelEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.chat.C;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Model;
import obfuscate.world.HubMap;
import obfuscate.world.MapManager;
import obfuscate.world.TempMap;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Model(name = "hub")
public class Hub extends SyncableObject implements CustomListener
{
    private TempMap tempMap;
    private List<StrikePlayer> players = new ArrayList<>();

    public Hub() {
        tempMap = MapManager.loadMap(new HubMap("hub"));
    }

    public TempMap getTempMap() {
        return tempMap;
    }

    public void join(StrikePlayer player) {
        players.add(player);
        player.getPlayer().teleport(tempMap.getWorld().getSpawnLocation());
        player.getPlayer().setGameMode(GameMode.ADVENTURE);
        new PlayerJoinHubEvent(this, player).trigger();
        player.updateTabName(null);

        var compass = new ItemStack(Material.COMPASS);
        var meta = compass.getItemMeta();
        meta.setDisplayName(C.cDRed + C.Bold + "Server List");
        compass.setItemMeta(meta);

        player.getPlayer().getInventory().setItem(0, compass);
    }

    public void leave(StrikePlayer player) {
        players.remove(player);
        new PlayerLeaveHubEvent(this, player).trigger();
    }

    @LocalEvent
    private void chat(PlayerChatEvent e) {

        String message = e.getPlayer().getFullChatName(null) + ChatColor.GRAY + " >> " + ChatColor.RESET + e.getPlayer().getRole().getChatMessageColor() + e.getMessage();

        for (StrikePlayer player : getPlayers()) {
            player.sendMessage(message);
        }
    }

    public Iterable<StrikePlayer> getPlayers() {
        return players;
    }

    @Override
    public Class<? extends ModelEvent<? extends SyncableObject>> getFulfilledEvent() {
        return null;
    }
}
