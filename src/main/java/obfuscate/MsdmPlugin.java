package obfuscate;

import obfuscate.account.FakeIdentity;
import obfuscate.comand.argument.*;
import obfuscate.comand.dev.StatesCommand;
import obfuscate.comand.game.VarCommand;
import obfuscate.comand.builder.CommandBuilder;
import obfuscate.comand.builder.CommandRegistry;
import obfuscate.comand.game.*;
import obfuscate.comand.npc.*;
import obfuscate.comand.owner.DisguiseCommand;
import obfuscate.comand.server.JoinCommand;
import obfuscate.comand.server.LeaveCommand;
import obfuscate.comand.server.PrivateMessageCommand;
import obfuscate.comand.server.RPCommand;
import obfuscate.comand.team.coach.CoachCommand;
import obfuscate.comand.team.coach.StartCoachCommand;
import obfuscate.comand.team.coach.UncoachCommand;
import obfuscate.comand.team.leader.TacPauseCommand;
import obfuscate.comand.team.leader.TeamLeaderCommand;
import obfuscate.config.ServerConfig;
import obfuscate.event.EventManager;
import obfuscate.event.GeneralServerEventHandler;
import obfuscate.event.custom.PluginManager;
import obfuscate.event.custom.intent.ChangePlayerBlacklistStatusAtGameIntent;
import obfuscate.event.custom.intent.ChangePlayerWhitelistStatusAtGameIntent;
import obfuscate.event.custom.intent.PlayerGenerateCodeIntent;
import obfuscate.event.custom.player.PlayerJoinServerEvent;
import obfuscate.event.custom.player.PlayerLeaveServerEvent;
import obfuscate.event.custom.server.ServerStartEvent;
import obfuscate.game.Server;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.core.GameInventory;
import obfuscate.game.core.IGame;
import obfuscate.game.debug.BulletLog;
import obfuscate.game.debug.ViewPlayer;
import obfuscate.game.debug.ViewRecorder;
import obfuscate.game.npc.BotManager;
import obfuscate.game.npc.PrerecordedPath;
import obfuscate.game.npc.trait.TargetPracticeTrait;
import obfuscate.game.player.BotPlayer;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.gamemode.registry.GameMode;
import obfuscate.message.MsgSender;
import obfuscate.message.MsgType;
import obfuscate.network.BackendEventManager;
import obfuscate.network.BackendManager;
import obfuscate.permission.Permission;
import obfuscate.podcrash.BMSChannel;
import obfuscate.team.InGameTeamData;
import obfuscate.team.StrikeTeam;
import obfuscate.ui.ServerList;
import obfuscate.ui.component.InventoryButton;
import obfuscate.ui.screen.BasicScreen;
import obfuscate.util.EventCollector;
import obfuscate.util.UtilEffect;
import obfuscate.util.Position;
import obfuscate.util.UtilParticle;
import obfuscate.util.chat.C;
import obfuscate.util.chat.MarkdownParser;
import obfuscate.util.chat.MessageBuilder;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.sidebar.UniqueSidebar;
import obfuscate.util.telegram.Telegram;
import obfuscate.util.time.Task;
import obfuscate.world.GameMap;
import obfuscate.world.MapData;
import obfuscate.world.MapManager;
import obfuscate.world.WorldTools;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.pengrad.telegrambot.TelegramBot;
import de.slikey.effectlib.util.ParticleEffect;
import net.citizensnpcs.api.CitizensAPI;
//import net.citizensnpcs.nms.v1_17_R1.entity.EntityHumanNPC;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaBook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MsdmPlugin extends JavaPlugin implements Listener
{
    private static MsdmPlugin instance;

    private BackendManager backend;

    private Server server;

    public static final ServerConfig Config = new ServerConfig();

    private static TelegramBot bot;

    public MsdmPlugin()
    {
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

    public static Logger logger() {
        return instance.getLogger();
    }
    public static void info(String string) {
        logger().info(C.BLACK_BRIGHT + string + C.RESET);
    }

    public static void highlight(String string) {
        logger().info(C.GREEN + string + C.RESET);
    }

    public static void warn(String string) {
        logger().warning(C.YELLOW_BOLD_BRIGHT + string + C.RESET);
    }

    public static void severe(String string) {
        logger().severe(C.RED_BACKGROUND_BRIGHT + string + C.RESET);
    }

    public static void important(String string) {
        logger().info(C.GREEN_BACKGROUND_BRIGHT + string + C.RESET);
    }


    private void serverOperational() {
        MsdmPlugin.logger().info("Server Is Loaded");

        for (Team t : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            MsdmPlugin.info("Unregister scoreboard team " + t.getName());
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
                MsdmPlugin.logger().log(Level.SEVERE, "Backend is down, shutting down");
                Bukkit.shutdown();
            }
            return isUp;
        });

        server.addHub();
        // record player looks to predict directions
        ViewRecorder.getInstance().start();
        MsdmPlugin.logger().info("Server Operational");
    }

    @Override
    public void onEnable()
    {
        server = new Server();
        backend = new BackendManager();

        MsdmPlugin.logger().info("Enabling Plugin");

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

        MsdmPlugin.logger().info("Registering commands");

        CommandRegistry.register(
                new CommandBuilder()
                .requirePlayer(true)
                .executor(ctx -> {
                    new ServerList(MsdmPlugin.getGameServer().getGames(), ctx.getPlayer()).show();
                    return true;
                })
                .build(),
                "menu"
        );

        CommandRegistry.register(
                new CommandBuilder()
                .requirePlayer(true)
                .executor(ctx -> {
                    if (ctx.getPlayer().hasEffect(PotionEffectType.NIGHT_VISION)) {
                        ctx.getPlayer().removeEffect(PotionEffectType.NIGHT_VISION);
                        ctx.getPlayer().sendMessage(MsgSender.SERVER, C.cGreen + "Night vision disabled");
                    } else {
                        ctx.getPlayer().addPotionEffect(PotionEffectType.NIGHT_VISION, 99999, 1);
                        ctx.getPlayer().sendMessage(MsgSender.SERVER, C.cGreen + "Night vision enabled");
                    }
                    return true;
                })
                .build(),
                "fullbright", "fb", "nv"
        );

        CommandRegistry.register(
                new CommandBuilder()
                .required(
                    new StrArg("player", "Player to receive the message").options(
                            ctx -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())
                    ),
                    new GreedyStr("message", "message")
                )
                .executor(ctx -> {
                    var receiver = Bukkit.getPlayer((String) ctx.getRequired("player"));
                    if (receiver == null) {
                        ctx.getPlayer().sendMessage(MsgSender.SERVER, C.cRed + "Player not found");
                        return true;
                    }
                    String message = ctx.getRequired("message");
                    receiver.sendMessage(C.cGold + C.Bold + ctx.getSender().getName() + " > Me: " + message);
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cGold + C.Bold + "Me > " + receiver.getName() + ": " + message);
                    return true;
                })
                .build(),
                "message", "msg", "m"
        );

        CommandRegistry.register(
                new CommandBuilder()
                        .required(
                                new GreedyStr("message", "message")
                        )
                        .executor(ctx -> {
                            String message = ctx.getRequired("message");
                            message = MarkdownParser.parse(message);
                            Bukkit.broadcastMessage(C.cGreen + "[" + C.cGray + "SHOUT" + C.cGreen + "] " + C.cGray + ctx.getSender().getName() + C.cGray + " >> " + C.cWhite + message);
                            return true;
                        })
                        .build(),
                "shout", "s"
        );

        CommandRegistry.register(
                new CommandBuilder()
                        .required(
                                new GreedyStr("message", "message")
                        )
                        .permission(
                                Permission.OPERATOR
                        )
                        .executor(ctx -> {
                            String message = ctx.getRequired("message");
                            message = MarkdownParser.parse(message);
                            Bukkit.broadcastMessage(C.cGreen + "[" + C.cGray + "BROADCAST" + C.cGreen + "] " + C.cGray + ctx.getSender().getName() + " >> " + C.cDGreen + message);
                            return true;
                        })
                        .build(),
                "broadcast", "bc"
        );

        CommandRegistry.register(
                new CommandBuilder()
                        .required(
                                new GreedyStr("message", "message")
                        )
                        .executor(ctx -> {
                            String message = ctx.getRequired("message");
                            Telegram.sendMessage("[!]" + ctx.getLabel() + " from " + ctx.getSender().getName() + ": " + message);
                            ctx.getSender().sendMessage(MsgSender.SERVER, C.cGreen + "Thanks for your message! It better be good, if you said something offensive I'm gonna fuck you up");
                            return true;
                        })
                        .build(),
                "bug", "feedback"
        );

        CommandRegistry.register(
                new CommandBuilder()
                .requirePlayer(true)
                .executor(ctx -> {
                    new PlayerGenerateCodeIntent(ctx.getPlayer()).trigger().thenSync(
                            intentResponse -> {
                                String message = intentResponse.getMessage();
                                ctx.getPlayer().sendMessage(MsgSender.PLUGIN, message);
                                return null;
                            }
                    );
                    return true;
                })
                .build(),
                "code"
        );

        /* Debug commands */
        CommandRegistry.register(
            new CommandBuilder()
                .requirePlayer(true)
                .permission(Permission.OPERATOR)
                .child("states",
                        new CommandBuilder()
                        .executor(new StatesCommand())
                )
                .child(
                        "games",
                        new CommandBuilder()
                        .executor(x -> {server.printState();return true;})
                )
                .child("inventory",
                    new CommandBuilder()
                        .executor(ctx -> {
                            IGame game = ctx.getPlayer().getGame();
                            GameInventory inv = game.getGameSession(ctx.getPlayer()).getInventory();
                            for (int slot = 0; slot < 9; slot ++) {
                                if (inv.hasItem(slot)) {
                                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "Slot " + slot + ": " + inv.getItem(slot).getName());
                                } else {
                                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "Slot " + slot + ": empty");
                                }
                            }
                            return true;
                        })
                )
                .child("team",
                    new CommandBuilder()
                        .executor(ctx -> {
                            IGame game = ctx.getPlayer().getGame();
                            InGameTeamData team = game.getPlayerRoster(ctx.getPlayer());
                            ctx.getSender().sendMessage(MsgSender.PLUGIN, "Side: " + team.getTeam().name());
                            ctx.getSender().sendMessage(MsgSender.PLUGIN, "Team: " + team.getName());
                            return true;
                        })
                )
                .child("view", new CommandBuilder()
                        .required(new StrArg("mode", "").options("normal", "predict"))
                        .executor(ctx -> {

                            boolean predict = ctx.getRequired("mode").equals("predict");

                            int predictTicks = 1;

                            if (ctx.getPlayer().getGame() != null) {
                                predictTicks = ctx.getPlayer().getGame().get(ConfigField.PREDICT_LOOK_TICKS);
                            }

                            int fPredictTicks = predictTicks;

                            for (int debugTick = 0; debugTick < 20 * 20; debugTick++) {
                                new Task(
                                        ()->{
                                            StrikePlayer player = ctx.getPlayer();
                                            Location loc = player.getEyeLocation();
                                            Vector view;

                                            if (predict) {
                                                view = player.getEyeLocation().getDirection().multiply(0.5);
                                            } else {
//                                                view = player.predictLook(player.getEyeLocation().getPitch(), player.getEyeLocation().getYaw()).multiply(0.5);
                                                view = player.predictLook(fPredictTicks);
                                            }
                                            // trace  to closest block
                                            for (int i = 0; i < 100; i++) {

                                                if (loc.clone().add(view).getBlock().getType().isSolid()) {
                                                    // schedule particle effect
                                                    for (int tick = 0; tick < 20 * 20; tick++) {
                                                        Location lc = loc.clone();
                                                        new Task(() -> {
                                                            // particle
                                                            UtilParticle.PlayParticle(ParticleEffect.REDSTONE, lc, 0,0,0, 0, 1);
                                                        }, tick).run();
                                                    }
                                                    break;
                                                }
                                                loc.add(view);
                                            }

                                        },
                                         debugTick
                                ).run();
                            }
                            return true;
                        })
                )
                    .child("recordedview",
                            new CommandBuilder()
                                    .required(new StrArg("filename", "Filename of recording"))
                                    .optional(new IntArg("shot", "index of the shot to replay"))
                                    .executor(ctx -> {
                                        StrikePlayer player = ctx.getPlayer();
                                        Integer shot = ctx.getOptional("shot");

                                        ArrayList<ArrayList<Vector>> recordedView = ViewRecorder.getShotHistory(ctx.getRequired("filename"));

                                        if (shot != null) {
                                            ViewPlayer.playSingleView("Selected Shot", player, recordedView.get(shot));
                                        } else {
                                            ViewPlayer.playAllViews(player, recordedView).thenSync(x -> {
                                                player.sendMessage(MsgSender.PLUGIN, "Done");
                                                return x;
                                            });
                                        }
                                        return true;
                                    })
                    )

                    .child("nearentity",
                        new CommandBuilder()
                            .executor(ctx -> {
                                StrikePlayer player = ctx.getPlayer();
                                Collection<Entity> entities = player.getWorld().getNearbyEntities(player.getLocation(), 10, 10, 10);
                                if (entities.isEmpty()) {
                                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "No entity nearby");
                                } else {
                                    for (Entity ent : entities) {
                                        ctx.getSender().sendMessage(MsgSender.PLUGIN, "Entity: " + ent);
                                    }
                                }
                                return true;
                            })
                    )
                    .child(
                            "hitreg",
                            new CommandBuilder()
                                    .required(new StrArg("player", "player whose shots to debug").options(
                                        ctx -> ctx.getPlayer().getGame().getOnlineDeadOrAliveParticipants().stream().map(StrikePlayer::getName).collect(Collectors.toList())
                                    ))
                                    .optional(
                                            new IntArg("shot", "index of shot to debug")
                                    )
                                    .executor(ctx -> {
                                        StrikePlayer player = ctx.getPlayer();
                                        String playerName = ctx.getRequired("player");
                                        Game game = player.getGame();
                                        StrikePlayer playerToDebug = game.getPlayer(playerName);
                                        Integer shotToDebug = ctx.getOptional("shot");
                                        if (shotToDebug == null) {
                                            shotToDebug = -1;
                                        }

                                        if (playerToDebug == null) {
                                            ctx.getSender().sendMessage(MsgSender.CMD, C.cRed + "Player not found");
                                            return false;
                                        }

                                        BulletLog log = game.getHitRegLog().getPlayerShot(playerToDebug, shotToDebug);
                                        if (log == null) {
                                            ctx.getSender().sendMessage(MsgSender.PLUGIN, "No such shot");
                                            return false;
                                        }
                                        UtilEffect.replayShot(log, 400);

                                        return true;
                                    })
                    )
                    .child(
                            "looklog",
                            new CommandBuilder()
                                    .required(new IntArg("duration", "duration in seconds"))
                                    .executor(ctx -> {
                                        StrikePlayer player = ctx.getPlayer();
                                        var rec = ViewRecorder.getInstance();
                                        int seconds = ctx.getRequired("duration");
                                        rec.recordFor(player);
                                        player.sendMessage(C.cGreen + "Recording view log for " + C.cYellow + seconds + C.cGreen + " seconds");
                                        new Task(() -> {
                                            rec.stopRecordingFor(player);
                                            player.sendMessage(C.cGreen + "Saved view log");
                                            player.playSound(Sound.ENTITY_PLAYER_LEVELUP);
                                        }, seconds * 20).run();
                                        return true;
                                    })
                    )
                    .child(
                            "reconnect",
                            new CommandBuilder()
                                    .executor(ctx -> {
                                        ctx.getSender().sendMessage(MsgSender.PLUGIN, "Reconnecting...");
                                        getBackend().getConnection().reconnect();
                                        ctx.getSender().sendMessage(MsgSender.PLUGIN, "Reconnected.");
                                        return true;
                                    })
                    )
                    .child(
                            "refresh",
                            new CommandBuilder()
                                    .executor(ctx -> {
                                        ctx.getSender().sendMessage(MsgSender.PLUGIN, "Refreshing models...");
                                        getBackend().refreshAllModels().thenSync(
                                            x -> {
                                                ctx.getSender().sendMessage(MsgSender.PLUGIN, "Refreshed.");
                                                return x;
                                            }
                                        );

                                        return true;
                                    })
                    )
                    .child(
                            "stun",
                            new CommandBuilder()
                                    .executor(ctx -> {
                                        StrikePlayer player = ctx.getPlayer();
                                        new Task(() -> player.stun(20 * 3), 20 * 5).run();
                                        return true;
                                    })
                    )
//                    .child(
//                            "movecontrol",
//                            new CommandBuilder()
//                            .executor(ctx -> {
//                                float var0 = ((int) ctx.getRequired("var0")) / 100f;
//                                float var1 = ((int) ctx.getRequired("var1")) / 100f;
//                                float var2 = ((int) ctx.getRequired("var2")) / 100f;
//                                float var3 = ((int) ctx.getRequired("var3")) / 100f;
//                                var npc = CitizensAPI.getDefaultNPCSelector().getSelected(ctx.getSender().sender);
//                                EntityHumanNPC handle = (EntityHumanNPC) ((CraftEntity) npc.getEntity()).getHandle();
//                                var move = handle.getMoveControl();
//
//                                move.a(var0, var1, var2, var3);
//                                move.a(0.1f, 0.1f);
//                                return true;
//                            })
//                            .required(
//                                    new IntArg("var0", ""),
//                                    new IntArg("var1", ""),
//                                    new IntArg("var2", ""),
//                                    new IntArg("var3", "")
//                            )
//                    )
//                    .child(
//                            "movecontrol2",
//                            new CommandBuilder()
//                                    .executor(ctx -> {
//                                        float var0 = ((int) ctx.getRequired("var0")) / 100f;
//                                        float var1 = ((int) ctx.getRequired("var1")) / 100f;
//                                        var npc = CitizensAPI.getDefaultNPCSelector().getSelected(ctx.getSender().sender);
//                                        EntityHumanNPC handle = (EntityHumanNPC) ((CraftEntity) npc.getEntity()).getHandle();
//                                        var move = handle.getMoveControl();
//
//                                        move.a(var0, var1);
//                                        return true;
//                                    })
//                                    .required(
//                                            new IntArg("var0", ""),
//                                            new IntArg("var1", "")
//                                    )
//                    )
//                    .child(
//                            "movemethod",
//                            new CommandBuilder()
//                                    .executor(ctx -> {
//                                        var npc = CitizensAPI.getDefaultNPCSelector().getSelected(ctx.getSender().sender);
//                                        EntityHumanNPC handle = (EntityHumanNPC) ((CraftEntity) npc.getEntity()).getHandle();
//                                        var move = handle.getMoveControl();
//
//                                        String method = ctx.getRequired("method");
//                                        ctx.getSender().sendMessage(MsgSender.PLUGIN, "method: " + method);
//                                        switch (method) {
//                                            case "a" -> move.a();
//                                            case "b" -> move.b();
//                                            case "c" -> move.c();
//                                            case "d" -> move.d();
//                                            case "e" -> move.e();
//                                            case "f" -> move.f();
//                                        }
//                                        return true;
//                                    })
//                                    .required(
//                                            new StrArg("method", "").options(
//                                                    "a",
//                                                    "b",
//                                                    "e",
//                                                    "c",
//                                                    "d",
//                                                    "f"
//                                            )
//                                            )
//                    )
                    .child(
                            "title",
                            new CommandBuilder()
                            .executor(ctx -> {
                                ctx.getSender().getPlayer().sendTitle(
                                        ctx.getRequired("title"),
                                        ctx.getRequired("subtitle"),
                                        ctx.getRequired("fadein"),
                                        ctx.getRequired("stay"),
                                        ctx.getRequired("fadeout")
                                );
                                return true;
                            })
                            .required(
                                    new StrArg("title", ""),
                                    new StrArg("subtitle", ""),
                                    new IntArg("fadein", ""),
                                    new IntArg("stay", ""),
                                    new IntArg("fadeout", "")
                            )
                    )
                    .child(
                           "bukkitteams",
                            new CommandBuilder()
                                .executor(ctx -> {
                                    var player = ctx.getSender().getPlayer();
                                    var board = player.getPlayer().getScoreboard();
                                    player.sendMessage("Scoreboard: " + board);
                                    player.sendMessage("Teams: ");
                                    for (Team team : board.getTeams()) {
                                        player.sendMessage("- " + team.getName() + " prefix: " + team.getPrefix() + "prefixedtext");
                                        for (String ent : team.getEntries()) {
                                            player.sendMessage("  * '" + ent + "'");
                                        }
                                    }
                                    player.sendMessage("Objectives: ");
                                    for (Objective objective : board.getObjectives()) {
                                       player.sendMessage("- " + objective.getName());
                                       player.sendMessage("  Scores:");
                                        for (String ent : objective.getScoreboard().getEntries()) {
                                             player.sendMessage("  * '" + ent + "' = " + objective.getScore(ent).getScore());
                                        }
                                    }
                                    return true;
                                })

                    )

                .permission(Permission.DEBUG)
                .build(),
                "debug"
        );

        CommandRegistry.register(
            new CommandBuilder()
                .description("game management commands")
                .executor(new GameCommand())
                .notImplemented()
                .requirePlayer(true)
                .child(
                    "whitelist",
                    new CommandBuilder()
                    .required(
                            StrArg.of(
                                    "action",
                                    "Action to do with player in whitelist"
                            )
                            .options(
                                    "add",
                                    "remove"
                            ),
                            StrArg.of(
                                    "player", "Player to whitelist"
                            )
                            .options(ctx -> Bukkit.getOnlinePlayers().stream()
                                    .map(Player::getName)
                                    .collect(Collectors.toList())
                            )
                    )
                    .executor(ctx -> {
                        String action = ctx.getRequired("action");
                        String playerName = ctx.getRequired("player");

                        if (action == null || playerName == null) {
                            return true;
                        }

                        Player player = Bukkit.getPlayer(playerName);

                        if (player == null) {
                            ctx.getSender().sendMessage(MsgSender.CMD, C.cRed + "Player not found");
                            return true;
                        }

                        StrikePlayer strikePlayer = StrikePlayer.getOrCreate(player);
                        Competitive game = (Competitive) ctx.getPlayer().getGame();

                        new ChangePlayerWhitelistStatusAtGameIntent(
                                strikePlayer,
                                ctx.getPlayer(),
                                game,
                                action.equals("add")
                        )
                        .trigger()
                        .thenSync(
                            response -> {
                                if (response.isSuccess()) {
                                    ctx.getSender().sendMessage(MsgSender.CMD, C.cGreen + response.getMessage());
                                } else {
                                    ctx.getSender().sendMessage(MsgSender.CMD, C.cRed + response.getMessage());
                                }
                                return response;
                            }
                        );
                        return true;
                    })
                )
                .child(
                    "blacklist",
                        new CommandBuilder()
                                .required(
                                    StrArg.of(
                                            "action",
                                            "Action to do with player in blacklist"
                                    )
                                    .options(
                                            "add",
                                            "remove"
                                    ),
                                    StrArg.of(
                                            "player", "Player to perform action on"
                                    ).options(
                                            ctx -> Bukkit.getOnlinePlayers().stream()
                                            .map(Player::getName)
                                            .collect(Collectors.toList())
                                    )
                                )
                                .optional(
                                    GreedyStr.of(
                                            "reason",
                                            "Reason for blacklisting"
                                    )
                                )
                                .executor(
                                    ctx -> {
                                        String playerName = ctx.getRequired("player");
                                        String action = ctx.getRequired("action");
                                        String reason = ctx.getOptional("reason");

                                        Player player = Bukkit.getPlayer(playerName);

                                        if (player == null) {
                                            ctx.getSender().sendMessage(MsgSender.CMD, C.cRed + "Player not found");
                                            return true;
                                        }

                                        StrikePlayer strikePlayer = StrikePlayer.getOrCreate(player);

                                        Competitive game = (Competitive) ctx.getSender().getPlayer().getGame();

                                        boolean isBlacklist = action.equals("add");

                                        new ChangePlayerBlacklistStatusAtGameIntent(
                                                strikePlayer,
                                                ctx.getPlayer(),
                                                game,
                                                isBlacklist,
                                                reason
                                        ).trigger().thenSync(
                                            intentResponse -> {
                                                if (intentResponse.isSuccess()) {
                                                    ctx.getSender().sendMessage(MsgSender.CMD, C.cGreen + intentResponse.getMessage());

                                                    // Kick player if blacklisted
                                                    if (isBlacklist && strikePlayer.isOnline() && strikePlayer.getGame() == game) {

                                                        // Leave
                                                        game.tryLeavePlayer(strikePlayer).thenSync(
                                                            (x) -> {
                                                                MsdmPlugin.getGameServer().getFallbackServer().join(strikePlayer);

                                                                String blacklistReason = reason == null ? "" : " Reason: " + reason;

                                                                game.broadcast(
                                                                        MsgSender.SERVER,
                                                                        C.cYellow + strikePlayer.getName() +
                                                                                " has been blacklisted from this game by " +
                                                                                ctx.getSender().getName() + "." +
                                                                                blacklistReason,
                                                                        MsgType.CHAT
                                                                );

                                                                String kickMessage = "You have been blacklisted from this game.";

                                                                if (reason != null && reason.length() != 0) {
                                                                    kickMessage += " " + C.cYellow + "Reason: " + reason;
                                                                }
                                                                strikePlayer.sendMessage(MsgSender.SERVER, C.cRed + kickMessage);
                                                                return x;
                                                            }
                                                        );
                                                    }
                                                } else {
                                                    ctx.getSender().sendMessage(MsgSender.CMD, C.cRed + intentResponse.getMessage());
                                                }
                                                return intentResponse;
                                            }
                                        );
                                        return true;
                                    }
                                )
                )
                .child(
                    "bot",
                    new CommandBuilder()
                    .child(
                        "add",
                        new CommandBuilder()
                        .executor(
                            ctx -> {
                                StrikePlayer player = ctx.getPlayer();
                                Game game = player.getGame();

                                int i = new Random().nextInt(1000);
                                BotPlayer bot = BotManager.createBot("Bot" + i, new TargetPracticeTrait("TargetPracticeTrait"));

                                int humanTeam = game.get(ConfigField.HUMAN_TEAM);
                                StrikeTeam botTeam;

                                if (humanTeam == 2) {
                                    botTeam = null;
                                } else {
                                    botTeam = StrikeTeam.values()[game.get(ConfigField.HUMAN_TEAM)].getOpposite();
                                }
                                bot.getInitializationPromise().thenAsync(

                                    x -> game.tryJoinPlayer(bot, botTeam, false).thenSync(
                                        response -> {
                                            MsdmPlugin.highlight("Bot join response: " + response.getMessage());
                                            return response;
                                        }
                                    )
                                );
                                ctx.getSender().sendMessage(MsgSender.PLUGIN, "Added bot");
                                return true;
                            }
                        )
                    )
                )
                .child(
                        "id",
                        new CommandBuilder()
                        .executor(ctx -> {
                            var game = ctx.getPlayer().getGame();
                            ctx.getSender().sendMessage(
                                    MsgSender.SERVER,
                                    "Game ID: " + game.getId().getObjId()
                            );
                            return true;
                        })

                )
                .child("forcestart",
                    new CommandBuilder()
                    .permission(Permission.MANAGE_GAMES)
                    .required(
                        new StrArg("delay", "delay before start")
                    )
                    .executor(new ForceStartCommand())
                )
                .child("pause",
                    new CommandBuilder()
                    .permission(Permission.MANAGE_GAMES)
                    .executor(new PauseCommand())
                )
                .child("unpause",
                    new CommandBuilder()
                    .permission(Permission.MANAGE_GAMES)
                    .executor(new UnpauseCommand())
                )
                .child("restart",
                    new CommandBuilder()
                    .permission(Permission.MANAGE_GAMES)
                    .optional(
                        new StrArg("delay", "delay before restart")
                    )
                    .executor(new RestartCommand())
                )
                .child("skip",
                    new CommandBuilder()
                    .permission(Permission.MANAGE_GAMES)
                    .executor(new SkipCommand())
                )
                .child("sub",
                    new CommandBuilder()
                    .permission(Permission.MANAGE_GAMES)
                    .required(
                        new StrArg(
                                "IGN",
                                "who to sub"
                        ).options(
                            state -> state.getSender().getPlayer().getGame()
                            .getOnlineDeadOrAliveParticipants().stream()
                            .map(x -> x.getName())
                            .collect(Collectors.toList())
                        ).validator(
                            ctx -> ctx.getSender().getPlayer().getGame().getPlayer(ctx.getValue()) == null ? "Player not found" : null
                        ),
                        new StrArg(
                                "team",
                                "short team name"
                        ).options(
                            state -> {
                                IGame game = state.getPlayer().getGame();
                                return Arrays.stream(game.getRosters()).map(InGameTeamData::getName).collect(Collectors.toList());
                            }
                        )
                    )
                    .executor(new SubCommand())
                )
                .child("swap",
                    new CommandBuilder()
                    .permission(Permission.MANAGE_GAMES)
                    .executor(new SwapCommand())
                )
                .child("var",
                    new CommandBuilder()
                    .executor(new VarCommand())
                    .required(
                        new StrArg("key", "field name").options(
                            Arrays.stream(ConfigField.values()).map(ConfigField::getName).collect(Collectors.toList())
                        )
                    )
                    .optional(
                        new IntArg("value", "field value")
                    )
                    .description("set or get a game variable")
                    .permission(Permission.CONFIG_GAME)
                )
                .child("leave",
                    new CommandBuilder()
                    .executor(new LeaveCommand())
                )
                .child(
                    "plugins",
                    new CommandBuilder()
                    .executor(ctx -> {
                        ctx.getSender().sendMessage(
                                MsgSender.PLUGIN,
                                "Plugins: " + ctx.getSender().getPlayer().getGame()
                                    .getPlugins().stream()
                                    .map(x -> x.getName())
                                    .collect(Collectors.joining(", "))
                        );
                        return true;
                    })
                )
                .child("editor",
                    new CommandBuilder()
                    .permission(Permission.MAINTAINER)
                    .child(
                        "refresh",
                        new CommandBuilder()
                        .executor(
                            ctx -> {
                                for (GameMap map : MapManager.getTemplateCollection().all()) {
                                    map.refresh();
                                    ctx.getSender().sendMessage(MsgSender.PLUGIN, "Refreshed map " + map.getName());
                                }
                                return true;
                            }
                        )
                    )
                    .child("position",
                        new CommandBuilder()
                                .required(new StrArg("type", "type of location").options("CT-SPAWN", "T-SPAWN", "DM-SPAWN"))
                        .executor(ctx -> {
                            StrikePlayer player = ctx.getPlayer();
                            Game game = player.getGame();
                            String type = ctx.getRequired("type");

                            Location loc = player.getLocation();
                            Position pos = new Position(loc);
                            MapData map = game.getTempMap().getMapData();
                            switch (type) {
                                case "CT-SPAWN" -> {
                                    map.getTeamRespawns(StrikeTeam.CT).add(pos);
                                    if (game.is(ConfigField.SHOW_SPAWNS)) {
                                        WorldTools.showLocation(game.getTempMap().getWorld(), pos, StrikeTeam.CT);
                                    }
                                }
                                case "T-SPAWN" -> {
                                    map.getTeamRespawns(StrikeTeam.T).add(new Position(loc));
                                    if (game.is(ConfigField.SHOW_SPAWNS)) {
                                        WorldTools.showLocation(game.getTempMap().getWorld(), pos, StrikeTeam.T);
                                    }
                                }
                                case "DM-SPAWN" -> {
                                    map.getDMRespawns().add(new Position(loc));
                                    if (game.is(ConfigField.SHOW_SPAWNS)) {
                                        WorldTools.showLocation(game.getTempMap().getWorld(), pos, null);
                                    }
                                }
                            }
                            MapManager.saveGameData(map);
                            player.sendMessage(C.cGreen + "Location saved");
                            return true;
                        })
                    )
                    .child("boundingbox",
                        new CommandBuilder()
                            .child("tool",
                                new CommandBuilder()
                                .executor(ctx -> {
                                    ctx.getPlayer().getPlayer().getInventory().setItem(4, new ItemStack(Material.NETHERITE_AXE));
                                    return true;
                                })
                            )
                            .child(
                                "save",
                                new CommandBuilder()
                                .required(
                                    new StrArg("type", "type of bounding box")
                                            .options(
                                                    "T-SHOP",
                                                    "CT-SHOP",
                                                    "BOMB-SITE-A",
                                                    "BOMB-SITE-B"
                                            )
                                )
                                .executor(ctx -> {
                                    String selectionType = ctx.getRequired("type");
                                    StrikePlayer player = ctx.getPlayer();
                                    Game game = player.getGame();
                                    BoundingBox box = WorldTools.getSelection(player);

                                    if (box == null) {
                                        player.sendMessage(C.cRed + "No selection");
                                        return false;
                                    }

                                    switch (selectionType) {
                                        case "T-SHOP":
                                            MapData map = game.getTempMap().getMapData();
                                            map.getShop(StrikeTeam.T).getBoxes().add(box);
                                            MapManager.saveGameData(map);
                                            break;

                                        case "CT-SHOP":
                                            map = game.getTempMap().getMapData();
                                            map.getShop(StrikeTeam.CT).getBoxes().add(box);
                                            MapManager.saveGameData(map);
                                            break;

                                        case "BOMB-SITE-A":
                                            map = game.getTempMap().getMapData();
                                            map.getASite().getBoxes().add(box);
                                            MapManager.saveGameData(map);
                                            break;

                                        case "BOMB-SITE-B":
                                            map = game.getTempMap().getMapData();
                                            map.getBSite().getBoxes().add(box);
                                            MapManager.saveGameData(map);
                                            break;
                                    }

                                    WorldTools.unselect(player);
                                    player.sendMessage(C.cGreen + "Saved selection");

                                    return true;
                                })
                        )
                        .child(
                            "expand",
                            new CommandBuilder()
                                .required(
                                    new IntArg("amount", "amount to expand")
                                )
                                .optional(new StrArg("mode", "direction of expansion")
                                        .options("up", "down", "north", "south", "east", "west")
                                )
                                .executor(
                                    ctx -> {
                                        StrikePlayer player = ctx.getPlayer();

                                        BoundingBox box = WorldTools.getSelection(player);
                                        if (box == null) {
                                            player.sendMessage(C.cRed + "No selection");
                                            return false;
                                        }
                                        int amount = ctx.getRequired("amount");
                                        String mode = ctx.getOptional("mode");

                                        if (mode == null) {
                                            float pitch = player.getLocation().getPitch();
                                            float yaw = player.getLocation().getYaw();

                                            mode = WorldTools.getLookDirection(player);

                                            if (pitch > 45) {
                                                mode = "up";
                                            } else if (pitch < -45) {
                                                mode = "down";
                                            } else if (yaw > 45 && yaw < 135) {
                                                mode = "east";
                                            } else if (yaw > 135 && yaw < 225) {
                                                mode = "south";
                                            } else if (yaw > 225 && yaw < 315) {
                                                mode = "west";
                                            } else {
                                                mode = "north";
                                            }
                                        }

                                        switch (mode) {
                                            case "up" -> box = box.expand(0, 0, 0, 0, amount, 0);
                                            case "down" -> box = box.expand(0, amount, 0, 0, 0, 0);
                                            case "north" -> box = box.expand(0, 0, 0, 0, 0, amount);
                                            case "south" -> box = box.expand(0, 0, amount, 0, 0, 0);
                                            case "east" -> box = box.expand(amount, 0, 0, 0, 0, 0);
                                            case "west" -> box = box.expand(0, 0, 0, amount, 0, 0);
                                        }

                                        WorldTools.setSelection(player, box);
                                        player.sendMessage(C.cGreen + "Selection expanded " + C.cYellow + amount + C.cGreen + " blocks " + C.cYellow + mode);
                                        return true;
                                    }
                                )
                        )
                        .child(
                            "unselect",
                            new CommandBuilder()
                                    .executor(ctx -> {
                                        StrikePlayer player = ctx.getPlayer();
                                        WorldTools.unselect(player);
                                        return true;
                                    })
                        )
                    )
                )
                .build(),
                "game", "g"
        );


        CommandRegistry.register(
                new CommandBuilder()
                        .executor(new LeaveCommand())
                        .build(),
                "leave", "lv", "hub", "lobby"
        );

        CommandRegistry.register(
                new CommandBuilder()
                .child(
                    "create",
                    new CommandBuilder()
                    .executor(new CreateGameCommand())
                    .permission(Permission.MANAGE_GAMES)
                    .required(
                        new StrArg("map", "Map Name")
                        .options(
                            MapManager.getAvailableMaps().all().stream().map(GameMap::getName).collect(Collectors.toList())
                        ),
                        new StrArg("preset", "Game preset that maps to a list op in-game plugins.")
                        .options(GameMode.getGameModeNames())
                    )
                    .description("creates new game")
                )
                .child(
                    "delete",
                    new CommandBuilder()
                            .executor(ctx -> {
                                int id = ctx.getRequired("id");
                                Competitive game = MsdmPlugin.getGameServer().getGame(id);

                                if (game == null) {
                                    ctx.getPlayer().sendMessage(MsgSender.SERVER, "Game #" + id + " does not exist");
                                    return true;
                                }

                                MsdmPlugin.getGameServer().deleteGame(game).thenSync(
                                    intentResponse -> {
                                        if (intentResponse.isSuccess()) {
                                            ctx.getPlayer().sendMessage(
                                                    MsgSender.SERVER,
                                                    "Game #" + id + " deleted"
                                            );
                                        } else {
                                            ctx.getPlayer().sendMessage(
                                                    MsgSender.SERVER,
                                                    "Game #" + id + " could not be deleted: " + intentResponse.getMessage()
                                            );
                                        }
                                        return null;
                                    }
                                );
                                return true;
                            })
                            .permission(Permission.MANAGE_GAMES)
                            .required(
                                    new IntArg("id", "Game ID")
                            )
                            .description("delete existing game")
                )
                .child(
                    "list",
                        new CommandBuilder()
                        .executor(ctx -> {

                            if (MsdmPlugin.getGameServer().getGames().isEmpty()) {
                                ctx.getPlayer().sendMessage(MsgSender.SERVER, "There are no games available");
                                return true;
                            }

                            ctx.getPlayer().sendMessage(MsgSender.SERVER, "Available game instances:");

                            int i = 0;
                            for (Game game : MsdmPlugin.getGameServer().getGames()) {
                                i += 1;

                                int playerCount = game.getOnlinePlayers().size();
                                int maxPlayers = game.getMaxPlayers();

                                TextComponent index = new TextComponent(MsgSender.SERVER.form(
                                        i + ". game #" +
                                        game.getId().getObjId() + " - " + game.getGameMap().getName() +
                                        " (" + playerCount + "/" + maxPlayers + " players) ")
                                );
                                TextComponent clickable = new TextComponent("Join");
                                clickable.setUnderlined(true);
                                clickable.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                                clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + game.getId().getObjId()));
                                clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new ComponentBuilder("Click to join").color(net.md_5.bungee.api.ChatColor.BLUE).create())
                                );
                                ctx.getPlayer().getPlayer().spigot().sendMessage(index, clickable);

                                if (!game.getPlugins().isEmpty()) {
                                    ctx.getPlayer().sendMessage(MsgSender.SERVER, "Plugins installed: (" + game.getPlugins().size() + ")");

                                    for (Object plugin : game.getPlugins()) {
                                        ctx.getPlayer().sendMessage(MsgSender.SERVER, "- " + plugin.getClass().getSimpleName());
                                    }
                                    ctx.getPlayer().sendMessage(MsgSender.SERVER, "Game mode: " + game.getMode());
                                }
                                ctx.getPlayer().sendMessage("");
                            }
                            if (ctx.getPlayer().getPlayer().getInventory().getItem(0) != null && ctx.getPlayer().getPlayer().getInventory().getItem(0).getType() == Material.COMPASS) {
                                ctx.getPlayer().sendMessage(C.cRed + "For fuck's sake, there is a compass in your inventory, why don't you use it instead?");
                            } else {
                                ctx.getPlayer().sendMessage(C.cGray + "Really, /menu is better");
                            }
                            return true;
                        })
                        .requirePlayer(true)
                )
                .build(),
                "instance"
        );

        CommandRegistry.register(
                new CommandBuilder()
                .executor(new JoinCommand())
                .requirePlayer(true)
                .required(
                    new IntArg("id", "lobby id")
                )
                .optional(
                    new BoolArg("spec", "spectate"),
                    new StrArg("team", "team name").options(
                            Arrays.stream(StrikeTeam.values()).map(StrikeTeam::name).collect(Collectors.toList())
                    )
                )
                .build(),
                "join"
        );

        CommandRegistry.register(
                new CommandBuilder()
                        .executor(ctx -> {
                            String ent = ctx.getRequired("entity");
                            Integer id = ctx.getRequired("id");

                            var model = getBackend().getById(new ObjectId(id, ent, null));
                            if (model == null) {
                                ctx.getPlayer().sendMessage("No such entity");
                                return true;
                            }
                            ctx.getSender().sendMessage(MsgSender.PLUGIN, "Inspecting " + ent + " with id " + id);

                            var serialized = model.serialize();
                            for (String key : serialized.keySet()) {
                                Object value = serialized.get(key);
                                if (value instanceof ObjectId objectId) {
                                    TextComponent keyName = new TextComponent(ChatColor.GOLD + key + ChatColor.GRAY + ": ");
                                    TextComponent clickable = new TextComponent("" + ChatColor.RESET + objectId.getEntity() + "#" + objectId.getObjId());
                                    clickable.setUnderlined(true);
                                    clickable.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                                    clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inspect " + objectId.getEntity() + " " + objectId.getObjId()));
                                    ctx.getPlayer().getPlayer().spigot().sendMessage(keyName, clickable);
                                } else if (value instanceof List listValue) {
                                    ctx.getPlayer().sendMessage(ChatColor.GOLD + key + ChatColor.GRAY + ": " + ChatColor.RESET + "[");
                                    for (Object listItem : listValue) {
                                        if (!listValue.isEmpty() && listValue.get(0) instanceof ObjectId) {
                                            ObjectId listItemObj = (ObjectId) listItem;
                                            TextComponent clickable = new TextComponent("-" + listItemObj.getEntity() + "#" + listItemObj.getObjId());
                                            clickable.setUnderlined(true);
                                            clickable.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                                            clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/inspect " + listItemObj.getEntity() + " " + listItemObj.getObjId()));
                                            ctx.getPlayer().getPlayer().spigot().sendMessage(clickable);
                                        } else {
                                            ctx.getPlayer().sendMessage("- " + listItem);
                                        }
                                    }
                                    ctx.getPlayer().sendMessage("]");
                                }
                                else {
                                    ctx.getPlayer().sendMessage(ChatColor.GOLD + key + ChatColor.GRAY + ": " + ChatColor.RESET + serialized.get(key));
                                }

                            }
                            return true;
                        })
                        .permission(Permission.DEBUG)
                        .requirePlayer(false)
                        .required(
                                new StrArg("entity", "entity name"),
                                new IntArg("id", "object id")
                        )
                        .build(),
                "inspect"
        );

        CommandRegistry.register(
                new CommandBuilder()
                .executor(ctx -> {
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cYellow + C.Bold + "What's New:");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.3");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Added /rpon and /rpoff commands");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.4");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Added map Vertigo");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed team auto-balance on rejoin");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Added /teammenu to change teams");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.5");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed \"join next\" button");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed elo reward received by wrong team");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Added 1v1 ranked matches");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.6");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed 1v1 ranked bombsite restriction");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed team change in ranked");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.7");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed bomb defuse volume");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed entity hit lag");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed void damage");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed hanging entity breaking");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Reduced pistol cone of fire increase rate");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.8");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Added option to swap grenades in inventory");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Removed fixed grenade slots");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.9");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Added client integration, dm to get forge mod");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed 1.14+ sneaking");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.10");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed inventory grenade swapping and grenade dropping");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed hub chat duplication");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Added book on join for retards who can't read chat");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cAqua + C.Bold + "v0.1.11");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed games being stuck in infinite warmup");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed sniper shot delay");
                    ctx.getSender().sendMessage(MsgSender.NONE, C.cWhite + "- Fixed mirage vines");
                    ctx.getSender().sendMessage(MsgSender.NONE, "");
                    return true;
                })
                .build(),
                "changelog", "whatsnew"
        );

        CommandRegistry.register(
                new CommandBuilder()
                        .executor(new RPCommand())
                        .requirePlayer(true)
                        .build(),
                "rp", "rpon"
        );

        CommandRegistry.register(
                new CommandBuilder()
                        .executor(ctx -> {
                            var game = ctx.getPlayer().getGame();
                            if (game == null) {
                                ctx.getSender().sendMessage(MsgSender.SERVER, C.cRed + "You have to be in-game to use this command.");
                                return true;
                            }
                            var menu = new BasicScreen();

                            menu.setCloseOnClick(true);
                            menu.setTitle(C.cGold + C.Bold + "Team Menu");
                            // 0  1  2  3  4  5  6  7  8
                            // 9  10 11 12 13 14 15 16 17
                            // 18 19 20 21 22 23 24 25 26
                            menu.setSize(27);
                            menu.addButton(
                                12,
                                new InventoryButton(Material.RED_WOOL)
                                .setTitle(C.cRed + "Bombers")
                                .setLore(C.cYellow + "Click to join the Bombers")
                                .setOnClick(
                                    click -> {
                                        game.tryJoinPlayerWithMessages(ctx.getPlayer(), false, StrikeTeam.T);
                                        return null;
                                    }
                                )
                            );

                            menu.addButton(
                                14,
                                new InventoryButton(Material.LIGHT_BLUE_WOOL)
                                .setTitle(C.cAqua + "SWAT")
                                .setLore(C.cYellow + "Click to join the SWAT")
                                .setOnClick(
                                        click -> {
                                            game.tryJoinPlayerWithMessages(ctx.getPlayer(), false, StrikeTeam.CT);
                                            return null;
                                        }
                                )
                            );

                            menu.addButton(
                                22,
                                new InventoryButton(Material.GRAY_WOOL)
                                .setTitle(C.cWhite + "Spectators")
                                .setLore(C.cYellow + "Click to spectate")
                                .setOnClick(
                                        click -> {
                                            game.tryJoinPlayerWithMessages(ctx.getPlayer(), true, null);
                                            return null;
                                        }
                                )
                            );

                            menu.show(ctx.getPlayer());

                            return true;
                        })
                        .requirePlayer(true)
                        .build(),
                "teammenu"
        );
//
        CommandRegistry.register(
                new CommandBuilder()
                        .executor(ctx -> {
                            var player = ctx.getPlayer();
                            player.unloadResources();
                            return true;
                        })
                        .requirePlayer(true)
                        .build(),
                "rpoff"
        );

        CommandRegistry.register(
                new CommandBuilder()
                .executor(new TeamLeaderCommand())
                .requirePlayer(true)
                .permission(Permission.TEAM_LEADER)
                .child("tac",
                        new CommandBuilder().executor(new TacPauseCommand())
                )
                .build(),
                "team");

        CommandRegistry.register
                (new CommandBuilder()
                .executor(new CoachCommand())
                .permission(Permission.COACH)
                .requirePlayer(true)
                .child("start",
                        new CommandBuilder()
                        .executor(new StartCoachCommand())
                )
                .child("stop",
                        new CommandBuilder()
                        .executor(new UncoachCommand())
                )
                .build(), "coach");

        CommandRegistry.register(new CommandBuilder()
                .executor(new DisguiseCommand())
                .requirePlayer(true)
                .permission(Permission.OPERATOR)
                .build(),
                "disguise");

        CommandRegistry.register(new CommandBuilder()
                .requirePlayer(true)
                .executor(new ReadyCommand())
                .build(), "ready");

        CommandRegistry.register(new CommandBuilder()
                .executor(new PrivateMessageCommand())
                .required(
                    new StrArg("IGN", "target"),
                    new StrArg("message", "your message")
                )
                .build(), "message", "msg", "m");

        CommandRegistry.register(new CommandBuilder()
                .permission(Permission.NPC)
                .requirePlayer(true)
                .child("paths",
                        new CommandBuilder()
                        .executor(new NpcPathsCommand())
                )
                .child("delete",
                        new CommandBuilder()
                        .required(
                            new StrArg("name", "name of path to delete")
                        )
                        .executor(new NpcDeletePathCommand())
                )
                .child("play",
                        new CommandBuilder()
                        .required(
                            new StrArg("name", "name of path to delete")
                                    .options(ctx -> PrerecordedPath.getPathNames(ctx.getPlayer().getGame().getGameMap()))
                        )
                        .executor(new NpcPlayPathCommand())
                )
                .child("record",
                        new CommandBuilder()
                        .required(
                            new StrArg("name", "name of path to delete")
                        )
                        .executor(new NpcRecordPathCommand())
                )
                .child("rstop",
                        new CommandBuilder()
                        .executor(new NpcStopRecordCommand())
                )
                .child("pstop",
                        new CommandBuilder()
                        .executor(new NpcStopPathCommand())
                )
                .child("kick", new CommandBuilder()
                        .executor(new BotKickCommand())
                )
                .executor(new NpcCommand())
                .build(), "bot");

        CommandRegistry.register(
                new CommandBuilder()
                .requirePlayer(true)
                .child("perms",
                        new CommandBuilder()
                        .executor(ctx -> {
                            ctx.getSender().sendMessage(MsgSender.PLUGIN, "Your Permissions:");
                            ctx.getPlayer().getRole().getPermissions().forEach(
                                p -> ctx.getSender().sendMessage(MsgSender.PLUGIN, "- " + p.getName())
                            );
                            return true;
                        })
                )
                .build(),
                "my"
        );

        // disguise
//        FakeIdentity.fillCache();

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

    private void sendGreetMessage(StrikePlayer player) {

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
        player.getPlayer().getInventory().addItem(book);
        // open it
        player.getPlayer().openBook(book);


        player.sendMessage(ChatColor.GRAY + "Welcome to " + ChatColor.YELLOW + "BetterMS" + ChatColor.GRAY + "!");

        player.sendMessage("");

        player.sendMessage(C.cGray + "* Send feedback or bug report: " + C.cGreen + "/feedback" + C.cGray + " <message> " + C.cGreen + "/bug"  + C.cGray + " <message>");
        player.sendMessage(C.cGray + "^ Please do that as soon as you find something instead of complaining after, that's the only way to improve the game");
        player.sendMessage(C.cGray + "* " +  C.cGreen + "/(s)hout " + C.cGray + "<message> - talk to the entire server, your usual chat is limited to your lobby");
        player.sendMessage(C.cGray + "* " +  C.cGreen + "/menu " + C.cGray + " if your hub compass is gone, or you are in game");
        player.sendMessage(C.cGray + "To play a ranked game (5v5/1v1), you may use the website.");
        player.sendMessage(C.cGray + "If You have any questions, contact me at Discord: obfuscqted");

        player.sendMessage("");

        player.sendMessage(ChatColor.GRAY + "Here are some useful tools and shortcuts:");
        var rpButton = MessageBuilder.of(C.cGreen + "[ Load RP ]").command("/rp").hover("Click to load RP");
        var converterButton = MessageBuilder.of(C.cGreen + "[ Converter ]")
                .link("http://207.244.252.241:9000/")
                .hover("Adds new sounds and textures\nrequired by this server\nto your existing MS resourcepack.");

        var website = MessageBuilder
                .of(C.cGreen + "[ Stats Website ]")
                .link("http://us.betterms.odays.ky/")
                .hover("Contains your statistics\nfor all games you played\non the server");

        var changelogButton = MessageBuilder.of(C.cGreen + "[ What's New ]").command("/changelog").hover("Click to see what's new");

        player.sendMessage(rpButton, MessageBuilder.of(" "), converterButton, MessageBuilder.of(" "), website, MessageBuilder.of(" "), changelogButton);

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

        e.setJoinMessage(C.cYellow + player.getName() + " Joined the Server");

        new Task(() -> sendGreetMessage(player), 1).run();
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

        if (!player.isDisguised())
            e.setQuitMessage(C.cYellow + player.getName() + " Left the Server");
        else
            e.setQuitMessage("");

        player.stopDisguise();

        new PlayerLeaveServerEvent(player).trigger();
    }

    @Override
    public void onDisable()
    {

        for (Player player : Bukkit.getOnlinePlayers())
        {
            player.kickPlayer(String.valueOf(ChatColor.GREEN) + ChatColor.BOLD + "Server is restarting");
        }
        MapManager.unloadAllTemp();

        CitizensAPI.getNPCRegistry().deregisterAll();
    }

}
