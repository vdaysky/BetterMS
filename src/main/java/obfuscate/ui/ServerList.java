package obfuscate.ui;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.gamemode.registry.GameMode;
import obfuscate.message.MsgSender;
import obfuscate.util.Promise;
import obfuscate.util.chat.C;
import obfuscate.util.time.Task;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ServerList {

    private final List<Competitive> games;
    private final StrikePlayer player;

    private static final HashSet<StrikePlayer> inConnectProgress = new HashSet<>();

    private final HashMap<Integer, Game> gamesBySlot = new HashMap<>();
    private static final HashMap<Inventory, ServerList> inventories = new HashMap<>();

    public static void onInventoryClick(Inventory inventory, Integer slot, boolean isRightClick) {
        var serverList = inventories.get(inventory);

        if (serverList == null) {
            return;
        }

        serverList.onClick(slot, isRightClick);
    }

    public static void onInventoryClose(Inventory inventory) {
        inventories.remove(inventory);
    }

    public ServerList(List<Competitive> games, StrikePlayer player) {
        this.games = games;
        this.player = player;
    }

    public void onClick(Integer slot, boolean isRightClick) {
        var game = gamesBySlot.get(slot);
        if (game == null) return;

        var inv = player.getPlayer().getOpenInventory();

        if (inv != null) {
            inv.close();
        }

        if (inConnectProgress.contains(player)) {
            player.sendMessage(MsgSender.SERVER, C.cRed + "You are already being connected to a game. Please wait.");
            return;
        }

        player.sendMessage(MsgSender.SERVER, C.cGray + "Connecting you to the game...");


        Promise<?> fut = game.tryJoinPlayer(player, isRightClick);

        if (fut == null) {
            return;
        }
        inConnectProgress.add(player);

        fut.thenSync((x) -> {
            inConnectProgress.remove(player);
            return x;
        });

        new Task(() -> {
            if (inConnectProgress.remove(player)) {
                player.sendMessage(MsgSender.SERVER, C.cRed + "Connection timed out.");
            }
        }, 20 * 15).run();
    }

    public void show() {
        Inventory handle = Bukkit.createInventory(null, 54, C.cYellow + C.Bold + "Server List");
        inventories.put(handle, this);

        List<Game> privateWhitelisted = new ArrayList<>();
        List<Game> pubs = new ArrayList<>();
        List<Game> dms = new ArrayList<>();
        List<Game> duels = new ArrayList<>();
        List<Game> gungames = new ArrayList<>();

        for (Game game : games) {
            if (game.getMode() == GameMode.PUB) {
                pubs.add(game);
            }
            else if (game.getMode() == GameMode.DEATHMATCH) {
                dms.add(game);
            }
            else if (game.getMode() == GameMode.DUEL) {
                duels.add(game);
            }
            else if (game.getMode() == GameMode.GUNGAME) {
                gungames.add(game);
            } else if (game.getWhitelist().contains(player)) {
                privateWhitelisted.add(game);
            }
        }

        // first row - shows private games
        for (int i = 0; i < Math.min(9, privateWhitelisted.size()); i++) {
            var game = privateWhitelisted.get(i);
            var icon = createGameIcon(game, player);
            gamesBySlot.put(i, game);
            handle.setItem(i, icon);
        }

        // second row - shows pubs
        for (int i = 0; i < Math.min(9, pubs.size()); i++) {
            var game = pubs.get(i);
            var icon = createGameIcon(game, player);
            gamesBySlot.put(i + 9, game);
            handle.setItem(i + 9, icon);
        }

        // third row - shows dms
        for (int i = 0; i < Math.min(9, dms.size()); i++) {
            var game = dms.get(i);
            var icon = createGameIcon(game, player);
            gamesBySlot.put(i + 18, game);
            handle.setItem(i + 18, icon);
        }

        // fourth row - shows duels
        for (int i = 0; i < Math.min(9, duels.size()); i++) {
            var game = duels.get(i);
            var icon = createGameIcon(game, player);
            gamesBySlot.put(i + 27, game);
            handle.setItem(i + 27, icon);
        }

        // fifth row - shows gungames
        for (int i = 0; i < Math.min(9, gungames.size()); i++) {
            var game = gungames.get(i);
            var icon = createGameIcon(game, player);
            gamesBySlot.put(i + 36, game);
            handle.setItem(i + 36, icon);
        }

        player.getPlayer().openInventory(handle);
    }

    private static ItemStack createGameIcon(Game game, StrikePlayer player) {
        var icon = new ItemStack(game.getMode().getIcon());
        var meta = icon.getItemMeta();

        ArrayList<String> lore = new ArrayList<>();

        if (game.getWhitelist().contains(player)) {
            lore.add(C.cGreen + C.Bold + "You are whitelisted");
        }

        lore.add(C.cYellow + "Click to join");

        lore.addAll(List.of(game.getMode().getDescription()));

        String iconName = C.cWhite + game.getMode().getGameName() + " #" + game.getId().getObjId();
        iconName += " " + game.getGameMap().getName();
        iconName += " (" + game.getOnlinePlayers().size() + "/" + game.getMaxPlayers() + ")";

        meta.setDisplayName(iconName);
        meta.setLore(lore);
        icon.setItemMeta(meta);
        icon.setAmount(Math.max(game.getOnlineDeadOrAliveParticipants().size(), 1));
        return icon;
    }
}
