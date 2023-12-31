package obfuscate;

import com.google.cloud.logging.LoggingHandler;
import com.google.cloud.logging.LoggingOptions;
import obfuscate.config.ServerConfig;
import obfuscate.event.EventManager;
import obfuscate.event.GeneralServerEventHandler;
import obfuscate.event.custom.PluginManager;
import obfuscate.event.custom.player.PlayerJoinServerEvent;
import obfuscate.event.custom.player.PlayerLeaveServerEvent;
import obfuscate.event.custom.server.ServerStartEvent;
import obfuscate.game.Server;
import obfuscate.game.core.Game;
import obfuscate.game.debug.ViewRecorder;
import obfuscate.game.player.StrikePlayer;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;
import obfuscate.network.BackendEventManager;
import obfuscate.network.BackendManager;
import obfuscate.podcrash.BMSChannel;
import obfuscate.util.EventCollector;
import obfuscate.util.chat.C;
import obfuscate.util.chat.Message;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.sidebar.UniqueSidebar;
import obfuscate.util.time.Task;
import obfuscate.world.MapManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.pengrad.telegrambot.TelegramBot;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.logging.Level;

public class MsdmPlugin extends JavaPlugin implements Listener
{
    private static MsdmPlugin instance;

    private BackendManager backend;

    private Server server;

    public static final ServerConfig Config = new ServerConfig();

    private static TelegramBot bot;


    public MsdmPlugin()
    {
        getLogger().addHandler(
                new LoggingHandler(
                        "BukkitLogger",
                        Logger.options
                )
        );

        Logger.info("Plugin is being loaded", Tag.SERVER_LIFECYCLE);
        instance = this;
        PluginManager.collectPluginClasses();
        BackendEventManager.collectEventClasses();
        bot = new TelegramBot(Config.getTelegramToken());
    }

    public static TelegramBot getTgBot() {
        return bot;
    }

    public static BackendManager getBackend() {
        return MsdmPlugin.getInstance().backend;
    }

    public static Server getGameServer() {
        return MsdmPlugin.getInstance().server;
    }

//    public static Logger logger() {
//        return new Logger();
//    }
//     public static void info(String string) {
//        logger().info(C.BLACK_BRIGHT + string + C.RESET);
//    }
//
//    public static void highlight(String string) {
//        logger().info(C.GREEN + string + C.RESET);
//    }
//
//    public static void warn(String string) {
//        logger().warning(C.YELLOW_BOLD_BRIGHT + string + C.RESET);
//    }
//
//    public static void severe(String string) {
//        logger().severe(C.RED_BACKGROUND_BRIGHT + string + C.RESET);
//    }
//
//    public static void important(String string) {
//        logger().info(C.GREEN_BACKGROUND_BRIGHT + string + C.RESET);
//    }


    private void serverOperational() {
        Logger.info("Server is operational", Tag.SERVER_LIFECYCLE);

        for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            t.unregister();
        }

        backend.connect();

        new ServerStartEvent(1).trigger();

        server.setId(
            new ObjectId(
                1,
                "Server",
                List.of(
                    new ObjectId(null, "Game", null)
                )
            )
        );

        backend.isHTTPUp().thenSync(isUp -> {
            if (!isUp) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.kickPlayer("Backend is down");
                }
                Logger.severe("Backend is down, shutting down", Tag.SERVER_LIFECYCLE);
                Bukkit.shutdown();
            }
            return isUp;
        });

        server.addHub();
        // record player looks to predict directions
        ViewRecorder.getInstance().start();
    }

    @Override
    public void onEnable()
    {
        server = new Server();
        backend = new BackendManager();

        Logger.info("Enabling Plugin", Tag.SERVER_LIFECYCLE);

        // Disable footsteps
        ProtocolLibrary.getProtocolManager().addPacketListener(
            new PacketAdapter(this, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                public void onPacketSending(PacketEvent event) {
                    PacketContainer packet = event.getPacket();
                    Sound sound = packet.getSoundEffects().read(0);

                    if (sound.getKey().getKey().endsWith(".step")) {
                        event.setCancelled(true);
                    }
                }
            }
        );
        // add packet listener for block interact packet
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(this, PacketType.Play.Client.USE_ITEM) {
                    public void onPacketSending(PacketEvent event) {
                    }

                    public void onPacketReceiving(PacketEvent event) {
                        StrikePlayer player = StrikePlayer.getOrCreate(event.getPlayer());
                        EventCollector.record(player, "PacketType.Play.Client.USE_ITEM");
                    }
                }
        );

        // register podcrash channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "podcrash:client");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "bms:bms", new BMSChannel());


        // Run task with delay 0, effectively waiting for full server load.
        // this is necessary because on this step games are loaded, and games
        // require to create worlds, which can only be done once server is fully
        // operational.
        new Task(this::serverOperational, 0).run();

        ServerCommands.registerAll();

        // listeners
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new GeneralServerEventHandler(), this);
        EventManager.construct();
    }

    public void updateHeaderFooter()
    {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            StrikePlayer s = StrikePlayer.getOrCreate(player);

            s.setTabHeaderFooter("§6Welcome to §9§lBetter§4§lMS",
                    ChatColor.DARK_GREEN + "there are " + ChatColor.YELLOW + (Bukkit.getOnlinePlayers().size()-1) +
                            ChatColor.DARK_GREEN + " other players online");
        }
    }

    public static MsdmPlugin getInstance()
    {
        return instance;
    }

    public static void sendGreetMessage(StrikePlayer player) {

        // open it
        var book = player.getPlayer().getInventory().getItem(1);
        if (book != null) {
            player.getPlayer().openBook(book);
        }

        player.sendMessage(Message.n().gray("Welcome to ").yellow("BetterMS").gray("!"));

        player.sendMessage("");

        player.sendMessage(Message.n().gray("* Send feedback or bug report: ").green("/feedback").gray(" <message> ").green("/bug").gray( " <message>"));
        player.sendMessage(Message.n().gray("^ Please do that as soon as you find something instead of complaining after, that's the only way to improve the game"));
        player.sendMessage(Message.n().gray("* ").green("/(s)hout ").gray("<message> - talk to the entire server, your usual chat is limited to your lobby"));
        player.sendMessage(Message.n().gray("* ").green("/menu ").gray(" if your hub compass is gone, or you are in game"));
        player.sendMessage(Message.n().gray("To play a ranked game (5v5/1v1), you may use the website."));
        player.sendMessage(Message.n().gray("If You have any questions, contact me at Discord: obfuscqted"));

        player.sendMessage("");

        player.sendMessage(Message.n().gray("Here are some useful tools and shortcuts:"));
        var rpButton = Message.n().green("[ Load RP ]").command("/rp").hover("Click to load RP");
        var converterButton = Message.n().green("[ Converter ]")
                .link("http://207.244.252.241:9000/")
                .hover("Adds new sounds and textures\nrequired by this server\nto your existing MS resourcepack.");

        var website = Message.n()
                .green("[ Stats Website ]")
                .link("http://us.betterms.odays.ky/")
                .hover("Contains your statistics\nfor all games you played\non the server");

        var changelogButton = Message.n().green("[ What's New ]").command("/changelog").hover("Click to see what's new");

        player.sendMessage(rpButton, Message.space(), converterButton, Message.space(), website, Message.space(), changelogButton);

        player.sendMessage("");
        player.sendMessage(C.cYellow + "GL&HF!");
        player.sendMessage("");
    }

    @EventHandler
    private void playerJoin(PlayerJoinEvent e)
    {
        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());

        player.clearBukkitInventory();

        player.waitFullyInitialized().thenSync(
            (x) -> new PlayerJoinServerEvent(player).trigger()
        );

        e.setJoinMessage(C.cYellow + player.getName() + " joined the server");
    }

    @EventHandler
    private void playerQuit(PlayerQuitEvent e)
    {
        StrikePlayer player = StrikePlayer.getOrCreate(e.getPlayer());
        UniqueSidebar.deregisterBoard(player);
        Game game = player.getGame();

        // update player count in footer
        new Task(this::updateHeaderFooter, 1).run();

        // when player leaves minecraft they effectively leave current game
        if (game != null)
            game.tryLeavePlayer(player);

        e.setQuitMessage(C.cYellow + player.getName() + " left the server");

        new PlayerLeaveServerEvent(player).trigger();
    }

    @Override
    public void onDisable()
    {
        getGameServer().shutdown();
        for (Player player : Bukkit.getOnlinePlayers())
        {
            player.kickPlayer(String.valueOf(ChatColor.GREEN) + ChatColor.BOLD + "Server is restarting");
        }
        MapManager.unloadAllTemp();

        CitizensAPI.getNPCRegistry().deregisterAll();
    }

}
