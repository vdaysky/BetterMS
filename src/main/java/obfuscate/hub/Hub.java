package obfuscate.hub;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import obfuscate.Lobby;
import obfuscate.event.CustomListener;
import obfuscate.event.LocalEvent;
import obfuscate.event.custom.game.PlayerChatEvent;
import obfuscate.event.custom.lobby.PlayerJoinHubEvent;
import obfuscate.event.custom.lobby.PlayerLeaveHubEvent;
import obfuscate.event.custom.network.ModelEvent;
import obfuscate.game.player.StrikePlayer;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;
import obfuscate.util.chat.C;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Model;
import obfuscate.world.HubMap;
import obfuscate.world.MapManager;
import obfuscate.world.TempMap;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaBook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Model(name = "hub")
public class Hub extends SyncableObject implements CustomListener, Lobby
{
    private TempMap tempMap;
    private Set<StrikePlayer> players = new HashSet<>();

    public Hub() {
        tempMap = MapManager.loadMap(new HubMap("hub"));
    }

    public TempMap getTempMap() {
        return tempMap;
    }

    public void join(StrikePlayer player) {
        if (players.contains(player)) {
            Logger.warning("Player " + player + " tried to join hub twice", player, Tag.POTENTIAL_BUG, Tag.LOBBY);
            return;
        }
        players.add(player);
        player.getPlayer().teleport(tempMap.getWorld().getSpawnLocation());
        player.getPlayer().setGameMode(GameMode.ADVENTURE);
        new PlayerJoinHubEvent(this, player).trigger();
        player.updateTabName(null);
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

    private ItemStack setupBook() {
        // open a book with text
        var book = new ItemStack(Material.WRITTEN_BOOK);
        var meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Welcome to BetterMS");
        meta.setAuthor("obfuscqted");
        List<String> pages;
        try {
            var field = CraftMetaBook.class.getDeclaredField("pages");
            field.setAccessible(true);
            pages = new ArrayList<>();
            field.set(meta, pages);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        TextComponent title = new TextComponent("Welcome to BetterMS!\n");
        TextComponent rankedText = new TextComponent(
                "" + net.md_5.bungee.api.ChatColor.BLACK + net.md_5.bungee.api.ChatColor.BOLD + "* Ranked\n" +
                        net.md_5.bungee.api.ChatColor.BLACK + " We have ranked queues for 5v5 and 1v1 games. To join a queue, visit our website.\n"
        );
        TextComponent websiteTitle = new TextComponent(
                "" + net.md_5.bungee.api.ChatColor.BLACK + net.md_5.bungee.api.ChatColor.BOLD + "* Website "
        );
        TextComponent websiteText = new TextComponent(
                net.md_5.bungee.api.ChatColor.BLACK + " Queue for ranked games, view your stats, and more.\n"
        );
        TextComponent converterTitle = new TextComponent(
                "" + net.md_5.bungee.api.ChatColor.BLACK + net.md_5.bungee.api.ChatColor.BOLD + "* RPack"
        );

        TextComponent converterText = new TextComponent(
                net.md_5.bungee.api.ChatColor.BLACK + " Manage SRP with /rpon or /rpoff. You may use any existing MS pack."
        );
        TextComponent websiteLink = new TextComponent(
                new ComponentBuilder(net.md_5.bungee.api.ChatColor.DARK_GREEN + "" + net.md_5.bungee.api.ChatColor.BOLD + "[ CLICK HERE ]\n")
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GOLD + "Our website. You can queue for ranked and view stats here.").create()))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://us.betterms.odays.ky"))
                        .create()
        );
        TextComponent converterLink = new TextComponent(
                new ComponentBuilder(net.md_5.bungee.api.ChatColor.DARK_GREEN + "" + net.md_5.bungee.api.ChatColor.BOLD + " [ CONVERT ]\n")
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GOLD + "Resourcepack Converter. Enhances your MS packs with BMS additions.").create()))
                        .event(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://207.244.252.241:9000/"))
                        .create()
        );
        TextComponent changelogButton1 = new TextComponent(
                new ComponentBuilder(net.md_5.bungee.api.ChatColor.DARK_GREEN + "" + net.md_5.bungee.api.ChatColor.BOLD + "[ CHANGELOG ]\n")
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(net.md_5.bungee.api.ChatColor.GOLD + "View Changelog").create()))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/changelog"))
                        .create()
        );
        pages.add(ComponentSerializer.toString(title, changelogButton1, rankedText, websiteTitle, websiteLink, websiteText, converterTitle, converterLink, converterText));
        book.setItemMeta(meta);
        return book;
    }

    @Override
    public void setupInventory(StrikePlayer player) {
        var compass = new ItemStack(Material.COMPASS);
        var meta = compass.getItemMeta();
        meta.setDisplayName(C.cDRed + C.Bold + "Server List");
        compass.setItemMeta(meta);

        player.getPlayer().getInventory().setItem(0, compass);

        var book = setupBook();
        player.getPlayer().getInventory().setItem(1, book);
    }
}
