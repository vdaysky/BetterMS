package obfuscate.game.core;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.bukkit.BulletHitEvent;
import obfuscate.event.bukkit.BulletHitPlayerEvent;
import obfuscate.event.custom.CancellableEvent;
import obfuscate.event.custom.backend.player.PlayerGameConnectEvent;
import obfuscate.event.custom.config.ConfigFieldChangeEvent;
import obfuscate.event.custom.gamestate.*;
import obfuscate.event.custom.intent.CreateGameIntentEvent;
import obfuscate.event.custom.intent.PlayerJoinGameIntentEvent;
import obfuscate.event.custom.damage.*;
import obfuscate.event.custom.game.*;
import obfuscate.event.custom.intent.PlayerLeaveGameIntentEvent;
import obfuscate.event.custom.item.*;
import obfuscate.event.custom.item.grenade.GrenadeThrowEvent;
import obfuscate.event.custom.item.gun.bullet.BulletStopEvent;
import obfuscate.event.custom.item.gun.bullet.BulletWallbangEvent;
import obfuscate.event.custom.player.*;
import obfuscate.event.custom.ready.ParticipantsReadyEvent;
import obfuscate.event.custom.ready.PlayerReadyEvent;
import obfuscate.event.custom.round.RoundWinEvent;
import obfuscate.event.custom.session.KdUpdateEvent;
import obfuscate.event.custom.session.PlayerPostStatusChangeEvent;
import obfuscate.event.custom.shop.PlayerShopEvent;
import obfuscate.event.custom.team.PlayerJoinRosterEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.plugins.IPlugin;
import obfuscate.game.core.traits.SharedGameContext;
import obfuscate.game.damage.*;
import obfuscate.game.dataregistry.DataKey;
import obfuscate.game.dataregistry.DataScope;
import obfuscate.game.dataregistry.ResetSignal;
import obfuscate.game.dataregistry.ScopedDataRegistry;
import obfuscate.game.debug.HitRegLog;
import obfuscate.game.npc.trait.StrikeTrait;
import obfuscate.game.player.*;
import obfuscate.game.restore.RestoreManger;
import obfuscate.game.sound.GameSoundManager;
import obfuscate.game.sound.TeamRadio;
import obfuscate.game.state.*;
import obfuscate.gamemode.Competitive;
import obfuscate.mechanic.item.StrikeStack;
import obfuscate.mechanic.item.armor.StrikeArmor;
import obfuscate.mechanic.item.guns.GunType;
import obfuscate.mechanic.item.melee.Knife;
import obfuscate.mechanic.item.objective.Bomb;
import obfuscate.mechanic.item.objective.Compass;
import obfuscate.mechanic.item.objective.MiscItemType;
import obfuscate.mechanic.item.utility.grenade.*;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.guns.Bullet;
import obfuscate.mechanic.version.LocationRecorder;
import obfuscate.mechanic.version.hitbox.CustomHitboxV1;
import obfuscate.mechanic.version.hitbox.Hitbox;
import obfuscate.mechanic.version.hitbox.SniperReducedHitbox;
import obfuscate.message.MsgSender;
import obfuscate.message.MsgType;
import obfuscate.network.models.schemas.GameData;
import obfuscate.network.models.responses.IntentResponse;
import obfuscate.podcrash.ACFeature;
import obfuscate.team.*;
import obfuscate.ui.component.HotbarButton;
import obfuscate.util.*;
import obfuscate.util.block.UtilBlock;
import obfuscate.util.chat.C;
import obfuscate.util.chat.ChatTable;
import obfuscate.util.chat.Format;
import obfuscate.util.java.Pair;
import obfuscate.util.recahrge.Recharge;
import obfuscate.util.sidebar.UniqueSidebar;
import obfuscate.util.time.Scheduler;
import obfuscate.util.time.Task;
import obfuscate.util.time.Time;
import obfuscate.world.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.citizensnpcs.api.trait.Trait;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static obfuscate.event.LocalPriority.*;

public abstract class Game extends GameData implements IGame {
    private final BiMap<Entity, StrikeItem> droppedItems = HashBiMap.create();
    private final HashMap<StrikePlayer, ArrayList<StrikePlayer>> spectatorsOnTarget = new HashMap<>();

    private final ArrayList<Task> roundTaskRegistry = new ArrayList<>();

    protected final HashMap<StrikePlayer, Integer> roundKillStreak = new HashMap<>();

    protected TempMap _tempMap;
    protected GameMap _gameMap;

    private final HitRegLog _hitRegLog = new HitRegLog();

    private final DamageManager damageManager = new DamageManager(this);
    private final RestoreManger Restore = new RestoreManger();
    private final DamageLog damageLog = new DamageLog();
    private final GameSoundManager soundManager = new GameSoundManager(this);

    private final SharedGameContext traits = new SharedGameContext();

    private AtomicLong v1_8_spacerBarExpiresAt = new AtomicLong(0);
    private BossBar v1_8_spacerBar;

//    private GameStateManager stateManager;

    private final TeamRadio TRadio = new TeamRadio(this, StrikeTeam.T);
    private final TeamRadio CTRadio = new TeamRadio(this, StrikeTeam.CT);

    private final HashMap<StrikePlayer, Boolean> readyList = new HashMap<>();

    private int roundTime = 0;

    private final BossBar bossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SEGMENTED_20);

    private boolean inProgress = false; // is the game in progress?
    private boolean ended = false;

    // whether game has finished all internal initializations and is ready to accept connections.
    // used by time event propagation to prevent game from being updated too early
    private boolean isInitialized = false;

    private Task highlightBoxesTask = null;

    private final ScopedDataRegistry dataRegistry = new ScopedDataRegistry();

    private GameStateInstance currentGameState = new GameStateInstance("Warmup", GeneralGameStage.WARM_UP);
    private int currentGameStateDuration = -1;

    Scoreboard teamAScoreboard;

    Scoreboard teamBScoreboard;

    Objective teamAHealthObjective;

    Objective teamBHealthObjective;


    public void setGameState(GameStateInstance i) {
//        MsdmPlugin.highlight("Set game state to " + i.getGeneralStage().getNiceName());
        currentGameState = i;
        currentGameStateDuration = getConfig().getDuration(i.getGeneralStage());
    }

    public void setGameState(GeneralGameStage stage) {
        setGameState(new GameStateInstance(stage.getNiceName(), stage));
        currentGameStateDuration = getConfig().getDuration(stage);
    }

    public void setGameState(GeneralGameStage stage, Integer duration) {
        setGameState(new GameStateInstance(stage.getNiceName(), stage));
        currentGameStateDuration = duration;
    }

    public GameStateInstance getGameState() {
        return currentGameState;
    }

    public int getGameStateDuration() {
        return currentGameStateDuration;
    }

    /** Defusal mode */

    public Bomb getBomb() {
        return getSharedContext().getBomb();
    }

    @Nullable
    public StrikePlayer getDefuser() {
        return getSharedContext().getDefuser();
    }

    @Nullable
    public StrikePlayer getBombCarry() {
        return getSharedContext().getBombCarry();
    }

    public ScopedDataRegistry getDataRegistry() {
        return dataRegistry;
    }

    /** Player Management */

    // list of active spectators in game. updated when player joins or leaves.
    // spectators are not handled by backend, so they are updated on mc side
    private final List<StrikePlayer> _spectators = new ArrayList<>();

    // all players that are in game world right now
    @Deprecated
    private final List<StrikePlayer> _onlinePlayers = new ArrayList<>();

    // list of all players that ever joined. this list will never decrease
    // TODO: try to replace with players that have game sessions which should be exactly same
    @Deprecated
    private final Set<StrikePlayer> _everPresentPlayers = new HashSet<>();

    private final HashSet<Grenade> thrownNades = new HashSet<>();

    public void initGame() {
        MsdmPlugin.highlight("initGame called");

        _gameMap = MapManager.getGameMap(getMapCodeName());

        if (_gameMap == null) {
            throw new RuntimeException("Map " + getMapCodeName() + " not found!");
        }

        _tempMap = MapManager.loadMap(_gameMap);
        new GameInitEvent(this).trigger();
        isInitialized = true;

        if (!getSharedContext().defersGameStart()) {
            startGame();
        }
    }

    public int getRoundKillStreak(StrikePlayer player) {
        return roundKillStreak.getOrDefault(player, 0);
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public List<BotPlayer> getBots() {
        return getOnlinePlayers().stream().filter(StrikePlayer::isBot).map(x -> (BotPlayer) x).collect(Collectors.toList());
    }

    @Override
    public Hitbox getPlayerHitbox(Bullet bullet) {
        // nerf snipers by reducing hitbox size
        if (bullet.getGun().getGunType() == GunType.SNIPER) {
            return new SniperReducedHitbox();
        }
        return new CustomHitboxV1();
    }

    @Override
    public void setGameState(GameStateInstance stage, Integer duration) {
        currentGameState = stage;
        currentGameStateDuration = duration;
    }

    @Override
    public int getStateConfigDuration() {
        return getConfig().getDuration(getGameState().getGeneralStage());
    }

    public Game() {

        // initialize game when model was fully loaded
        this.waitFullyInitialized().thenSync(
                x->{
                    // run pre init to allow plugins hook into game early to change core behaviours like game states
                    for (IPlugin<Competitive> plugin : getPlugins()) {
                        plugin.preInit((Competitive) this);
                    }
                    this.initGame();
                    return x;
                }
        );

        // todo cleanup this mess 銈銉鉰
        teamAScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        teamBScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (teamAScoreboard.getObjective("hp") == null) {
            teamAHealthObjective = teamAScoreboard.registerNewObjective("hp", "dummy");
            teamAHealthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            teamAHealthObjective.setDisplayName("HP");
        } else {
            teamAHealthObjective = teamAScoreboard.getObjective("hp");
            teamAHealthObjective.setDisplayName("HP");
            teamAHealthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
        teamBHealthObjective = teamAHealthObjective;
    }

    /** Go into round end state. If side swap, pause will be longer. */
    public void setRoundEndState() {
        MsdmPlugin.highlight("Should swap sides: " + shouldSwapSides());

        setGameState(GeneralGameStage.ROUND_END);

        // event (this event will add a time based win, shouldSwapSides can only be called after)
        new RoundTimerEndEvent(this).trigger();

        if (shouldSwapSides()) {
            setLeftDuration(20);
            getGameState().removeTag(StateTag.CAN_MOVE);
            getGameState().removeTag(StateTag.CAN_INTERACT);
            getGameState().removeTag(StateTag.DAMAGE_ALLOWED);
        }
    }

    @LocalEvent(priority = NATIVE)
    private void updateGameState(TimeEvent e) {
        if (e.getReason() != TimeEvent.UpdateReason.SECOND) {
            return;
        }

        if (!getGameState().is(StateTag.TICKABLE)) {
            return;
        }

        if (currentGameStateDuration == -1) {
            return;
        }

        getSharedContext().getGameStateUpdater().update(this);
        currentGameStateDuration--;
    }

    /* Player Management */

    @Override
    public Promise<? extends IntentResponse> tryLeavePlayer(StrikePlayer player) {

        _onlinePlayers.remove(player);

        MsdmPlugin.logger().info("Player " + player.getName() + " left the game.");

        if (getGameSession(player).getStatus() == PlayerStatus.PARTICIPATING) {
            if (!isEnded() && getGameSession(player).isAlive())
            {
                // actually kill player.
                // so much easier than simulating it.
                new PlayerDeathEvent(
                        this,
                        player,
                        player,
                        new DamageSourceWrapper(""),
                        DamageReason.NONE,
                        new DamageModifiers()
                ).trigger();
            }
        }

        if (getGameSession(player).getStatus() == PlayerStatus.SPECTATING) {
            _spectators.remove(player);
        }

        return new PlayerLeaveGameIntentEvent(this, player).trigger().thenSync(
            intentResponse -> {
                if (player.isOnline()) {
                    player.clearBukkitInventory();
                }
                return intentResponse;
            }
        );
    }

    @Override
    public Promise<? extends IntentResponse> tryJoinPlayer(StrikePlayer player, StrikeTeam team, boolean spec) {
        return new PlayerJoinGameIntentEvent(this, team, player, spec).trigger();
    }

    @Override
    public Promise<? extends IntentResponse> tryJoinPlayer(StrikePlayer player, boolean spec, StrikeTeam team) {
        return new PlayerJoinGameIntentEvent(this, team, player, spec).trigger();
    }

    public Promise<? extends IntentResponse> tryJoinPlayerWithMessages(StrikePlayer player, boolean spec, StrikeTeam team) {

        player.sendMessage(MsgSender.SERVER, C.cGray + "Connecting to game#" + getId().getObjId() + "...");

        if (canJoin(player, spec)) {
            return tryJoinPlayer(player, spec, team).thenSync(
                    e -> {
                        if (e.isSuccess()) {
                            player.sendMessage(MsgSender.SERVER, ChatColor.GREEN + e.getMessage());
                        } else {
                            player.sendMessage(MsgSender.SERVER, ChatColor.RED + e.getMessage());
                        }
                        return null;
                    }
            );
        }
        else {
            player.sendMessage(MsgSender.SERVER, ChatColor.RED + "You are not allowed to join this server!");
        }
        return null;
    }

    @Override
    public Promise<? extends IntentResponse> tryJoinPlayer(StrikePlayer player, boolean spec) {
        return tryJoinPlayer(player, spec, null);
    }

//    @Override
//    public GameStateManager getStateManager()
//    {
//        if (stateManager == null) {
//            MsdmPlugin.highlight("Initializing state manager with states " + getSharedContext().getStages());
//
//            // game will have stages defined in traits
//            // which makes it possible to change states in plugins on initialization
//            stateManager = new GameStateManager(
//                    this,
//                    getSharedContext().getStages()
//            );
//        }
//
//        return stateManager;
//    }

    @Override
    public String getGameName() {
        return getSharedContext().getModeName();
    }


    /** Shared context is meant to solve communication problem between plugins and game instance.
     * Plugins can push changes to context and game will be pulling data from it.
     * Reason for this change was need to store bomb holder in game instance so bomb is always accessible
     * by game no matter what game mode it is; But there is no public setter.
     * */
    @Override
    public SharedGameContext getSharedContext() {
        return traits;
    }

    public void removeBots() {
        for (StrikePlayer player : getOnlineDeadOrAliveParticipants()) {
            if (player instanceof BotPlayer) {
                tryLeavePlayer(player);
            }
        }
        // todo: kick Frosstt
    }

    /* Broadcasting to players */

    public void broadcast(MsgSender sender, String message, MsgType type, Object ... args)
    {
        for (StrikePlayer player : getOnlinePlayers())
        {
            if (type == MsgType.CHAT)
                player.getPlayer().sendMessage(sender.form(message));
            if (type == MsgType.HOTBAR)
                player.sendMessage(sender.form(message));
            if (type == MsgType.TITLE || type == MsgType.SUBTITLE)
            {
                int fadeIn = 0;
                int fadeOut = 0;
                int stay = 100;

                if (args.length > 0)
                {
                    if (args[0] instanceof Integer)
                    {
                        fadeIn = (int) args[0];
                    }
                    if (args.length > 1 && args[1] instanceof Integer)
                    {
                        stay = (int) args[1];
                    }
                    if (args.length > 2 && args[2] instanceof Integer)
                    {
                        fadeOut = (int) args[2];
                    }
                }

                if (type == MsgType.SUBTITLE)
                    player.sendTitle(" ", sender.form(message), fadeIn, stay, fadeOut);
                else
                    player.sendTitle(sender.form(message), " ", fadeIn, stay, fadeOut);
            }

            if (type == MsgType.BOSSBAR)
            {
                int percent = 100;
                if (args.length == 1 && args[0] instanceof Integer)
                    percent = (Integer) args[0];

                player.setBossbar(message, percent);
            }
        }
    }

    public String getPlayerTeamColor(StrikePlayer player) {
        var team = getPlayerTeam(player);

        if (team == null) {
            return null;
        }

        return team.getColor();
    }

    public String getPlayerOtherTeamColor(StrikePlayer player) {
        var team = getPlayerTeam(player);

        if (team == null) {
            return null;
        }

        return team.getOpposite().getColor();
    }

    public InGameTeamData getEnemyRoster(StrikePlayer player) {
        return getRoster(getGameSession(player).getRoster().getTeam().getOpposite());
    }

    @LocalEvent(priority = PRE_HIGH) // run before player health was reset
    private void onRoundEndShowDamageLog(RoundResetEvent e) {

        for (StrikePlayer player : getOnlineDeadOrAliveParticipants()) {

            var table = new ChatTable();

            table.addRow("Enemy", "Received from", "Dealt To", "Health");

            var session = getGameSession(player);
            for (OfflineStrikePlayer enemy : getRoster(session.getRoster().getTeam().getOpposite())) {

                if (!enemy.isOnline()) {
                    continue;
                }

                StrikePlayer onlineEnemy = (StrikePlayer) enemy;

                Pair<Integer, Double> receivedFrom = damageLog.getReceivedDamageFrom(onlineEnemy, player);
                Pair<Integer, Double> dealtTo = damageLog.getReceivedDamageFrom(player, onlineEnemy);
                double enemyHealth = onlineEnemy.getHealth(this);

                table.addRow(
                        onlineEnemy.getName(),
                        Math.round(receivedFrom.value()) + " in " + receivedFrom.key() + " hits",
                        Math.round(dealtTo.value()) + " in " + dealtTo.key() + " hits",
                        Format.Health(enemyHealth, 20, 10)
                );
            }

            for (ArrayList<String> row : table.alignAndPrepare()) {
                player.sendMessage(C.cGreen + String.join(" ", row));
            }
        }

        getDamageLog().reset();
    }

    @Override
    public void registerRoundTask(Task task)
    {
        roundTaskRegistry.add(task);
    }

    @Override
    public void stopTasks()
    {
        for (Task task : roundTaskRegistry)
        {
            if (task.isRunning())
                task.cancel();
        }
        roundTaskRegistry.clear();
    }

    @Override
    public void resetGameInfo()
    {
        // stop all tasks like fire spread of shop close event task
        stopTasks();

        // reset time counter
        roundTime = 0;

        // clear all grenades
        incendiary.clear();
        fireBlocks.clear();
        smokes.clear();

        // clear round stats
        roundKillStreak.clear();

        // delete all dropped guns
        deleteDropped();

        // delete grenades in air
        deleteNades();

        // remove items in fire
        InvulnerableItem.removeAll(this);

        // restore map
        Restore.RestoreAll();

        getDataRegistry().signal(ResetSignal.ROUND_END, null);

        for (StrikePlayer participant : getOnlineDeadOrAliveParticipants()) {
            pdcRefreshStatsOfPlayer(participant);
        }
    }

    @LocalEvent
    private void resetPlayerLifeDataOnDeath(PlayerDeathEvent e) {
        getDataRegistry().signal(ResetSignal.PLAYER_DEATH, e.getDamagee());

        pdcRefreshStatsOfPlayer(e.getDamagee());
        if (e.getDamager() != null) {
            pdcRefreshStatsOfPlayer(e.getDamager());
        }
    }

    public GameSoundManager getSoundManager() {
        return soundManager;
    }

    public TeamRadio getTRadio() {
        return TRadio;
    }

    public TeamRadio getCTRadio() {
        return CTRadio;
    }

    public TeamRadio getRadio(StrikeTeam team) {
        return team == StrikeTeam.T ? getTRadio() : getCTRadio();
    }

    @LocalEvent //TODO: move somewhere?
    private void freezeTimeEndSound(FreezeTimeEndEvent e) {
        getTRadio().roundStart();
        getCTRadio().roundStart();
        e.getGame().broadcast(
                MsgSender.NONE,
                C.cGreen + "Go!",
                MsgType.TITLE,
                0, 40, 0
        );
    }

    @LocalEvent
    private void roundWinSound(RoundWinEvent e) {

        int delay = 0;

        // if CTs won by objective, delay round win sound since it would overlap with bomb defuse sound
        if (e.getWinner().getTeam() == StrikeTeam.CT && e.getReason() == RoundWinEvent.Reason.OBJECTIVE) {
            delay = 40;
        }

        new Task(() -> {
            StrikeTeam winnerTeam = e.getWinner().getTeam();
            getRadio(winnerTeam).roundWin();
            getRadio(winnerTeam.getOpposite()).roundLoose();
        }, delay).run();
    }

    @LocalEvent
    private void stunOnHit(BulletHitPlayerEvent e) {
        if (e.getHitPlayer() == null)
            return;

        // don't stun self
        if (e.getHitPlayer() == e.getBullet().getShooter()) {
            return;
        }

        // player with kevlar don't get stunned
        if (e.getGame().getGameSession(e.getHitPlayer()).getInventory().hasKevlar()) {
            return;
        }

        var gun = e.getBullet().getGun();
        e.getGame().stunPlayer(e.getHitPlayer(), gun);
    }

    private void stunPlayer(StrikePlayer hitPlayer, Gun gun) {
        if (is(ConfigField.NEW_STUN)) {
            float mod = get(ConfigField.NEW_STUN_MODIFIER) / 100f;
            var seconds = gun.getGunType().getStunSeconds();
            hitPlayer.stun((int)(20 * seconds * mod));
        } else {
            hitPlayer.getPlayer().setVelocity(new Vector(0, 0, 0));
        }
    }

    @Override
    public void forceStart(int seconds)
    {
        MsdmPlugin.logger().info("Force start game in " + seconds + " seconds");
        // game is already started
        if (isInProgress()) {
            MsdmPlugin.logger().info("Game is already started");
            return;
        }

        broadcast(
                MsgSender.GAME,
                "Game will be force-started in " + C.cYellow + seconds + C.cWhite + " seconds",
                MsgType.CHAT
        );

        new Task(
                ()-> {
                    if (isInProgress()) {
                        return;
                    }
                    currentGameState = new GameStateInstance(
                            "FreezeTime",
                            GeneralGameStage.FREEZE_TIME
                    );
                },
                seconds * 20
        ).run();
    }

    @Override
    public InGameTeamData getOther(InGameTeamData roster) {
        if (getTeamA().getTeam() == roster.getTeam()) {
            return getTeamB();
        }
        return getTeamA();
    }


    @LocalEvent(priority = LocalPriority.PRE)
    private void joinPlayer(PlayerGameConnectEvent e) {
        if (!e.getPlayer().isOnline())
            return;

        joinPlayer(e.getPlayer(), e.getTeam(), e.isSpectating());
    }

    @LocalEvent(cascade = true)
    private void playerJoinRosterEvt(PlayerJoinGameEvent e)
    {
        ensureCoachSpectate();
        // only add boss bar to newer clients, prevent ugly via rewind's wither
        if (e.getPlayer().getVersion() > ProtocolVersion.v1_11_1.getVersion()) {
            bossBar.addPlayer(e.getPlayer().getPlayer());
        }
        e.getPlayer().updateTabName(this);
    }

    @LocalEvent(cascade = true)
    private void greetPlayerOnJoin(PlayerJoinGameEvent e)
    {
        String rosterName = "spectators";

        if (e.getRoster() != null) {
            rosterName = e.getRoster().getNiceName();
        }

        String suffix = "";

        if (e.getGame().getGameState().isWarmup()) {
            suffix = " (" + e.getGame().getOnlineDeadOrAliveParticipants().size() + "/" + e.getGame().getMaxPlayers() + ")";
        }

        e.getGame().broadcast(
                MsgSender.JOIN,
                e.getPlayer().getName() + ", " + rosterName + suffix,
                MsgType.CHAT
        );
    }

    @LocalEvent
    private void greetPlayerOnJoin(PlayerLeaveGameEvent e)
    {
        e.getGame().broadcast(
                MsgSender.LEAVE,
                e.getPlayer().getName(),
                MsgType.CHAT
        );
    }

    @LocalEvent(cascade = true, priority = LocalPriority.PRE)
    private void setScoreboardOnRosterJoin(PlayerJoinRosterEvent e) {

        if (e.getRoster() == null) {
            return;
        }

        if (e.getRoster().getTeam() == StrikeTeam.CT) {
            MsdmPlugin.highlight("Set scoreboard for " + e.getPlayer().getActualName() + " to teamAScoreboard");
            e.getPlayer().getPlayer().setScoreboard(teamAScoreboard);
            teamAHealthObjective.getScore(e.getPlayer().getActualName()).setScore(100);
            e.getPlayer().getPlayer().setScoreboard(teamAScoreboard);
        } else {
            MsdmPlugin.highlight("Set scoreboard for " + e.getPlayer().getActualName() + " to teamBScoreboard");
            e.getPlayer().getPlayer().setScoreboard(teamBScoreboard);
            teamBHealthObjective.getScore(e.getPlayer().getActualName()).setScore(100);
            e.getPlayer().getPlayer().setScoreboard(teamBScoreboard);
        }
    }

    public Scoreboard getTeamAScoreboard() {
        return teamAScoreboard;
    }

    public Scoreboard getTeamBScoreboard() {
        return teamBScoreboard;
    }

    public Objective getTeamAHealthObjective() {
        return teamAHealthObjective;
    }

    public Objective getTeamBHealthObjective() {
        return teamBHealthObjective;
    }

    private void ensureCoachSpectate()
    {
        for (StrikePlayer coach : getCoaches())
        {
            if (getSpectatedPlayer(coach) != null)
                continue;

            setRandomSpectateTarget(coach);
        }
    }

    @LocalEvent
    private void headshot(HeadshotEvent e)
    {
        if (e.getDamagee().getInventory(this).hasHelmet()) {
            getSoundManager().headshotThroughHelm().at(e.getDamagee().getEyeLocation()).play();
        }
        else {
            getSoundManager().headshot().at(e.getDamagee().getEyeLocation()).play();
        }
    }

    @LocalEvent
    private void knifeSound(MeleeDamageEvent e)
    {
        if (e.isBackStab())
            getSoundManager().knifeBackStab().at(e.getDamagee().getLocation()).play();
        else
            getSoundManager().knifeStab().at(e.getDamagee().getLocation()).play();
    }

    @LocalEvent
    private void onGameTerminate(GameTerminatedEvent e) {
        // stop all tasks like fire spread of shop close event task
        stopTasks();

        for (StrikePlayer player : getOnlinePlayers()) {
            tryLeavePlayer(player);
            MsdmPlugin.getGameServer().getFallbackServer().join(player);
            player.sendMessage(MsgSender.SERVER, C.cGold + "Game you were in was terminated by administrator. You were teleported to hub.");
        }

        getTempMap().getWorld().getPlayers().forEach(p -> p.kickPlayer(
                "Game terminated. You should've been teleported to fallback server automatically, but something went wrong."
        ));

        // delete the world
        getTempMap().unload();
    }

    @LocalEvent(priority = PRE_HIGH) // important event, as it marks player dead
    private void killPlayer(PlayerDeathEvent e)
    {
        // reset kill streak on death
        roundKillStreak.put(e.getDamagee(), 0);

        boolean suicide = e.getDamagee() == e.getDamager();

        boolean useIcons = e.getGame().is(ConfigField.USE_ICONS);

        e.getDamagee().getPlayer().setLevel(0);
        getGameSession(e.getDamagee()).setScoped(false);
        getGameSession(e.getDamagee()).setAlive(false);

        e.getDamagee().getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getDamagee().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 99, false, false));
        var sub = "";

        if (e.getDamager() != null) {
            sub = e.getDamager().getName() + " killed you with " + e.getDescription(useIcons);
        }
        e.getDamagee().sendTitle("You Died", sub, 5, 20, 5);

        // particle effect
        UtilEffect.playerDeath(e.getDamagee());

        // Clear inventory after death
        if (!getConfig().getValue(ConfigField.KEEP_INVENTORY).bool()) {
            dropInventory(e.getDamagee());
            getGameSession(e.getDamagee()).getInventory().clear();
        }

        String killFeed;

        if (e.getDamager() != null) {
            if (e.getGame().getConfig().getValue(ConfigField.DINK_ON_KILL).bool()) {
                if (e.getReason() == DamageReason.GUN) {
                    e.getDamager().playSound(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE);
                }
            }
            killFeed = e.getDamagee().getShortChatName(this) + C.cGray + " was killed by " +
                    e.getDamager().getShortChatName(this) +
                    C.cGray + (suicide ? "" : " with " + (useIcons ? C.Reset : C.cGreen) + e.getDescription(useIcons)) +  C.cGray + ".";

        }
        else
            killFeed = e.getDamagee().getShortChatName(this) + C.cGray + " was killed by " + (useIcons ? C.Reset : C.cGreen) + e.getDescription(useIcons) + C.cGray + ".";

        broadcast(
                MsgSender.GAME,
                killFeed,
                MsgType.CHAT
        );

        // bar expired
        if (v1_8_spacerBarExpiresAt.get() < System.currentTimeMillis()) {
            v1_8_spacerBar = broadcastBossBar("", -1);
            // do not show boss bar to players on newer versions, they already have a boss bar on top
            // that acts as a spacer
            for (StrikePlayer player : getOnlinePlayers()) {
                if (player.getVersion() > ProtocolVersion.v1_11_1.getVersion()) {
                    v1_8_spacerBar.removePlayer(player.getPlayer());
                }
            }
        }
        v1_8_spacerBarExpiresAt.set(System.currentTimeMillis() + 10000);

        new Task(() -> {
            if (System.currentTimeMillis() >= v1_8_spacerBarExpiresAt.get()) {
                v1_8_spacerBar.removeAll();
                v1_8_spacerBar.setVisible(false);
            }
        },
                10 * 20 + 1
        ).run();

        broadcastBossBar(" ".repeat(120) + killFeed, 10);
    }

    @Override
    public @Nullable StrikeTeam getPlayerTeam(StrikePlayer player) {
        var roster = getPlayerRoster(player);
        if (roster == null) {
            return null;
        }
        return roster.getTeam();
    }

    /** Get player's roster. Can be null in case player is spectating */
    @Override
    public @Nullable InGameTeamData getPlayerRoster(StrikePlayer player) {
        var session = getGameSession(player);
        if (session == null) {
            MsdmPlugin.warn("Player " + player + " does not have session in game " + this);
            return null;
        }
        return session.getRoster();
    }

    public boolean dropItem(StrikeItem item, @Nullable StrikeStack stack, StrikePlayer player, boolean natural, DropReason reason) {

        EventCollector.record(player, "dropItem");

        CancellableEvent e = new ItemDropEvent(this, item, stack, player, reason);
        e.trigger();

        if (e.isCancelled())
            return false;

        if (stack != null) {
            decreaseStack(player, stack, ItemLostFocusEvent.FocusLostReason.DROP);
        }

        item.drop(player, this, natural);

        new ItemPostDropEvent(this, item, stack, player, reason).trigger();
        return true;
    }

    @LocalEvent
    private void cancelDropsIfNotAllowed(ItemDropEvent e) {
        if (!is(ConfigField.ALLOW_ITEM_DROP)) {
            e.setCancelled(true);
        }
    }

    @Override
    public void dropInventory(StrikePlayer player) {
        if (away(player)) {
            return;
        }

        // Drop Primary Gun
        if (player.hasItem(this,0)) {
            var stack = player.getInventory(this).getStack(0);
            dropItem(stack.top(), stack, player, true, DropReason.DEATH);
        }

        // Drop Secondary Gun
        else if(player.hasItem(this, 1)) {
            var stack = player.getInventory(this).getStack(1);
            dropItem(stack.top(), stack, player, true, DropReason.DEATH);
        }

        // drop Grenade
        for (int i = 3; i < 6; i++) {
            if (player.hasItem(this, i))
            {
                var stack = player.getInventory(this).getStack(i);
                dropItem(stack.top(), stack, player, true, DropReason.DEATH);
                break;
            }
        }

        // Drop Equipment
        if (player.hasItem(this, 8)) {
            var stack = player.getInventory(this).getStack(8);
            var item = stack.top();

            if (!(item instanceof Compass)) {
                dropItem(item, stack, player, true, DropReason.DEATH);
            }
        }
    }

    @Override
    public void joinPlayer(StrikePlayer player, InGameTeamData team, boolean spectate)
    {
        boolean rejoined = _everPresentPlayers.contains(player);
        _everPresentPlayers.add(player);

        // save player as online, no matter if he spectates or not
        _onlinePlayers.add(player);

        MsdmPlugin.logger().info("Player " + player.getName() + " joined");
        if (spectate) {
            _spectators.add(player);
            MsdmPlugin.logger().info("Player " + player.getName() + " spectates");
        }
        else {
            // if player joins or rejoins - he is not ready
            readyList.put(player, false);
            MsdmPlugin.logger().info("Player " + player.getName() + " is not ready");
        }


        if (!rejoined)
        {
            // trigger Pre event
            new PlayerJoinGameEvent(this, player, spectate).trigger();
        }
        else
        {
            // trigger Pre event
            new PlayerReconnectEvent(this, player).trigger();

            MsdmPlugin.logger().info("Player " + player.getName() + " is reconnected");
        }

        getGameSession(player).setAlive(false);

        boolean willSpectate = !currentGameState.is(StateTag.JOINABLE) || spectate;

        // this will sync player's inventory with his session.
        // if he joined for first time, it will empty his inventory
        // spectators don't have inventories though
        if (!spectate) {
            getGameSession(player).getInventory().restore(this);
        }

        // respawn will respawn player depending on its state.
        // state was set already so everything gud
        respawn(player, willSpectate);

        if (rejoined)
        {
            // trigger post event
            new PlayerPostReconnectEvent(this, player, spectate).trigger();
        }
        else
        {
            // trigger post event
            new PlayerPostGameJoinEvent(this, player, spectate).trigger();
        }
    }

    @LocalEvent
    private void itemBuyEffects(PlayerShopEvent e) {

        if (e.isCancelled())
            return;

        e.getPlayer().sendMessage(MsgSender.GAME, C.cGray + "You equipped " + C.cGreen + e.getBoughtItem().getName() + C.cGray + ".");

        if (e.getBoughtItem() instanceof StrikeArmor) {
            e.getPlayer().makeSound(Sound.ENTITY_HORSE_ARMOR, 1.5f, 1f);
            return;
        }

        e.getPlayer().makeSound(Sound.ENTITY_ITEM_PICKUP, 1.5f, 1f);
    }

    public void decreaseStack(StrikePlayer player, StrikeStack stack, ItemLostFocusEvent.FocusLostReason reason) {
        if (stack.isEmpty()) {
            return;
        }

        StrikeItem item = stack.pop();

        if (stack.isEmpty()) {
            // trigger focus lost event
            new ItemLostFocusEvent(item, this, player, reason).trigger();
        }
    }

    public void throwGrenade(StrikePlayer player, boolean wasLeftClick) {

        StrikeStack stack = player.getHeldSlot(this);
        Grenade grenade = stack.top();

        boolean cancelled = new GrenadeThrowEvent(
            this, grenade, player, wasLeftClick
        ).triggerSync();

        if (cancelled) {
            return;
        }

        grenade.throwGrenade(player, wasLeftClick, this);

        boolean infiniteGrenades = this.getConfig().getValue(ConfigField.INFINITE_AMMO).val() > 0;

        this.decreaseStack(player, stack, ItemLostFocusEvent.FocusLostReason.CONSUME);

        if (infiniteGrenades) {
            stack.add(grenade.copy());
        }

    }

    @LocalEvent
    private void onClickThrowGrenade(PlayerClickEvent e) {
        // have to introduce this ugly fix because PlayerInteractEvent
        // is triggered twice when looking at block and holding food (any interactable?)
        if (!Recharge.done(e.getPlayer(), "ThrowGrenade", 40)) {
            return;
        }

        StrikeItem item = e.getPlayer().getHeldItem(this);

        if (!(item instanceof Grenade))
            return;

        e.getPlayer().throwGrenade(
                this,
                !e.isRightClick()
        );
    }

    @LocalEvent
    private void breakGlassWithKnife(PlayerClickEvent e) {
        if (e.isRightClick()) {
            return;
        }

        Block target = e.getPlayer().getPlayer().getTargetBlock(null, 2);

        if (!UtilBlock.isGlassPane(target))
            return;

        if (e.getPlayer().getHeldItem(this) instanceof Knife) {
            breakGlass(target);
        }
    }

    @LocalEvent
    private void reloadOnLeftClick(PlayerClickEvent e) {
        if (e.isRightClick()) {
            return;
        }

        if (e.getPlayer().getVersion() >= ClientVersion.V1_9.getProtocolVersion()) {
            // For some weird reason, the right click on a block in Adventure mode triggers
            // two PlayerAnimation events, while left click only one. Let's use it to our advantage
            // to recognize left clicks with 100% accuracy.
            // If player triggered two events within a tick we do nothing.
            // If player only triggered one, we can reload in the next tick.
            EventCollector.record(e.getPlayer(), "ArmSwing");

            Scheduler.runNextTick(
                    () -> {
                        if (EventCollector.count(e.getPlayer(), "ArmSwing", 1) != 1) {
                            return;
                        }



                        Gun gun = e.getPlayer().getGunInHand(this);

                        if (gun == null)
                            return;

                        gun.reload(this, e.getPlayer(), false);
                    }
            );
        } else {
            Scheduler.runNextTick(
                    () -> {
                        int packetCount = EventCollector.count(e.getPlayer(), "PacketType.Play.Client.USE_ITEM", 10);

                        if (packetCount > 0) {
                            // there was use item packet recorded. This means player right clicked, so reload should not happen.
                            // for some reason, right clicking block causes arm swing as well.
                            return;
                        }
                        Gun gun = e.getPlayer().getGunInHand(this);

                        if (gun == null)
                            return;

                        gun.reload(this, e.getPlayer(), false);
                    }
            );
        }
    }

    @LocalEvent
    private void soundOnItemPickUp(ItemPickUpEvent e) {

        if (e.isCancelled())
            return;

        // bomb has custom message
        if (e.getItem().getStats() != MiscItemType.BOMB) {
            e.getPlayer().sendMessage(MsgSender.GAME, C.cGray + "You picked up " + C.cGreen + e.getItem().getName() + C.cGray + ".");
        }

        e.getPlayer().makeSound(Sound.ENTITY_ITEM_PICKUP, 1.5f, 1f);
    }

    @Override
    public void updateGrenadesAndBullets()
    {
        //Grenades
        Iterator<Grenade> grenadeIterator = thrownNades.iterator();

        while (grenadeIterator.hasNext())
        {
            Grenade grenade = grenadeIterator.next();

            UtilParticle.PlayCrit(grenade.getLocation());

            if (getConfig().getValue(ConfigField.GRENADE_TRAJECTORY).bool()) {
                final Location l = grenade.getLocation();
                for (int tick = 0; tick < getConfig().getValue(ConfigField.GRENADE_TRAJECTORY_DURATION).val() * 20; tick++) {
                    new Task(
                            () -> UtilParticle.PlayCrit(l),
                            tick
                    ).run();
                }
            }

            //Expired
            if (grenade.getTicksLived() > 400)
            {
                grenade.removeEntity();
                grenadeIterator.remove();
                continue;
            }

            //Completed
            if (grenade.update(this))
            {
                grenade.removeEntity();
                grenadeIterator.remove();
            }
        }

        // incendiary
        Iterator<Location> fireIterator = incendiary.keySet().iterator();

        while (fireIterator.hasNext())
        {
            Location loc = fireIterator.next();

            if (incendiary.get(loc) < System.currentTimeMillis())
                fireIterator.remove();

            else
                getSoundManager().burn().at(loc).play();
        }
        // Smoke
        Iterator<Location> smokeIterator = smokes.keySet().iterator();

        while (smokeIterator.hasNext())
        {
            Location loc = smokeIterator.next();

            if (smokes.get(loc) < System.currentTimeMillis())
                smokeIterator.remove();

            else
                getSoundManager().fireExtinguish().at(loc).vol(0.1f).pitch(0.1f).play();
        }
    }

    @LocalEvent
    private void broadcastStartCancelMessage(GameStartCancelEvent e)
    {
        broadcast(MsgSender.GAME, "Game start cancelled", MsgType.CHAT);
    }

    @LocalEvent
    private void updateStats(PlayerDeathEvent e)
    {
        boolean suicide = e.getDamagee() == e.getDamager();

        if (e.getDamager() != null && !suicide) {
            getGameSession(e.getDamager()).addKill();
        }

        getGameSession(e.getDamagee()).addDeath();
    }

    /** Start main stage of game. Does not care about anything else, it just starts the game */
    @Override
    public void startGame()
    {
        // game already started
        if (isInProgress())
            return;

        MsdmPlugin.highlight("Game Started!");

        inProgress = true;

        new GameStartedEvent(this).trigger();
    }

    @LocalEvent(priority = LocalPriority.PRE)
    private void reset(GameStartedEvent e)
    {
        for (GameSession session : getGameSessions()) {
            // remove everything that might be left from warm up,
            // such as inventory, scope, k/d, respawn task, etc.
            session.reset();
        }

        for (StrikePlayer participant : this.getOnlineDeadOrAliveParticipants())
        {
            // game only handles respawns at the end of round,
            // so I have to respawn players here for the first time
            respawn(participant, false);
        }

        // general round stuff
        resetGameInfo();
    }

    @Override
    public boolean is(ConfigField field) {
        return getConfig().getValue(field).bool();
    }

    @Override
    public Integer get(ConfigField field) {
        return getConfig().getValue(field).val();
    }

    /** respawns are handled here. */
    @LocalEvent
    private void tryRespawn(PlayerDeathEvent e)
    {
        if (e.getDamagee().isNPC()) {
            BotPlayer bot = (BotPlayer) e.getDamagee();
            for (Trait trait : bot.getNPC().getTraits()) {
                if (trait instanceof StrikeTrait strikeTrait) {
                    strikeTrait.onDeath();
                }
            }
        }

        // Respawn as spectator
        respawn(e.getDamagee(), true);

        Task task = new Task(()->
        {
            if (!currentGameState.is(StateTag.RESPAWNABLE))
                return;

            if (away(e.getDamagee()))
                return;

            if(e.getDamagee().isAlive(this))
                return;

            respawn(e.getDamagee(), false);

        }, 20*3);

        e.getDamagee().setRespawnTask(this, task);
        task.run();
    }

    @Override
    public void updateSpectator(StrikePlayer spectator)
    {
        StrikePlayer target = getSpectatedPlayer(spectator);

        if (target==null)
            return;

        spectator.getPlayer().setSpectatorTarget(target.getPlayer());
    }

    @LocalEvent
    private void onItemDrop(ItemPostDropEvent e) {
        if (is(ConfigField.ITEM_DESTROY_ON_DROP)) {
            var dropped = e.getItem();
            deregisterDroppedItem(dropped);
        }
    }

    @LocalEvent
    private void updateSpectatorsOnTarget(PlayerDeathEvent e)
    {
        ArrayList<StrikePlayer> spectators = spectatorsOnTarget.get(e.getDamagee());

        if (spectators == null)
            return;

        InGameTeamData roster = getPlayerRoster(e.getDamagee());
        StrikePlayer target = getRandomAliveMember(roster);
        spectatePlayer(spectators, target);
    }

    @Override
    public StrikePlayer getSpectatedPlayer(StrikePlayer spectator)
    {
        for (StrikePlayer target : spectatorsOnTarget.keySet())
        {
            if (spectatorsOnTarget.get(target).contains(spectator) && !away(target))
                return target;
        }
        return null;
    }

    @Override
    public void spectateNext(StrikePlayer spectator)
    {
        StrikePlayer target = getSpectatedPlayer(spectator);
        StrikePlayer next = getNextTarget(target);

        if (next == null) {
            MsdmPlugin.warn("No next target found for " + spectator.getName());
            return;
        }

        MsdmPlugin.highlight("Spectating " + next.getName() + " for " + spectator.getName());
        setSpectatorTarget(spectator, next);
    }

    /** Find player that can be spectated after this player */
    private @Nullable StrikePlayer getNextTarget(StrikePlayer target)
    {
        var roster = getPlayerRoster(target);

        List<StrikePlayer> aliveTargets;

        if (roster != null) {
            aliveTargets = getAlivePlayers(roster);
        } else {
            aliveTargets = getAlivePlayers();
        }

        if (aliveTargets.isEmpty())
            return null;

        int i = 0;
        for (; i < aliveTargets.size(); i++) {
            if (aliveTargets.get(i) == target)
                break;
        }

        i = (i + 1) % aliveTargets.size();

        return aliveTargets.get(i);
    }

    void stopSpectatingPlayer(StrikePlayer player)
    {
        StrikePlayer target = getSpectatedPlayer(player);
        if (target == null)
            return;

        spectatorsOnTarget.get(target).remove(player);
    }

    void spectatePlayer(Iterable<StrikePlayer> spectators, @Nullable StrikePlayer target)
    {
        // clone iterable to prevent comod idk where it is
        Collection<StrikePlayer> copy = new ArrayList<>();

        for (StrikePlayer player : spectators) copy.add(player);

        for(StrikePlayer spectator : copy)
        {
            setSpectatorTarget(spectator, target);
        }
    }

    /** Sets spectator target for given player. Registers player as spectator of that target.
     *
     * @param spectator player that spectates target
     * @param target player that is being spectated, if null then player stops spectating
     * */
    void setSpectatorTarget(StrikePlayer spectator, @Nullable StrikePlayer target)
    {
        if(spectator == target) {
            System.out.println("[WARN] cant spectate self");
            return;
        }

        if(spectator.getPlayer().getGameMode() != GameMode.SPECTATOR)
        {
            System.out.println("[WARN] cant spectate when... not spectating");
            return;
        }

        // remove current target (if exists)
        stopSpectatingPlayer(spectator);

        if(target != null) {
            spectatorsOnTarget.putIfAbsent(target, new ArrayList<>());
            spectatorsOnTarget.get(target).add(spectator);
            spectator.getPlayer().setSpectatorTarget(target.getPlayer());
        } else {
            spectator.getPlayer().setSpectatorTarget(null);
        }
    }

    public void pdcAssignToTeam(StrikePlayer joined) {
        var session = getGameSession(joined);
        var roster = session.getRoster();

        if (roster == null) {
            return;
        }

        for (StrikePlayer player : getOnlinePlayers()) {
            player.pdcAssignTeam(joined, roster);
        }
    }

    public void pdcAddPlayerToTab(StrikePlayer joined) {
        pdcAssignToTeam(joined);
        pdcRefreshStatsOfPlayer(joined);
    }

    public void pdcRefreshStatsOfPlayer(StrikePlayer player) {
        for (StrikePlayer participant : getOnlineDeadOrAliveParticipants()) {
            setStats(player, participant);
        }
    }

    public void setStats(StrikePlayer receiver, StrikePlayer target) {
        var session = getGameSession(target);

        var K = session.getKills();
        var D = session.getDeaths();
        boolean positive = K >= D;
        var diff = Math.abs(session.getKills() - session.getDeaths());
        String color = positive ? C.cGreen : C.cRed;

        receiver.pdcSetTabVariable(target, "K-D", K + "-" + D);
        receiver.pdcSetTabVariable(target, "Diff", color + (positive ? "+": "-") + diff);
    }

    public void pdcRefreshStatsForPlayer(StrikePlayer player, InGameTeamData roster) {
        for (StrikePlayer memberA : getOnline(roster)) {
            player.pdcAssignTeam(memberA, roster);
            setStats(player, memberA);
        }
    }

    public void pdcRefreshTeams() {
        for (StrikePlayer player : getOnlinePlayers()) {
            for (StrikePlayer participant : getOnlineDeadOrAliveParticipants()) {
                player.pdcAssignTeam(participant, getGameSession(participant).getRoster());
            }
        }
    }

    public void pdcSyncClient(StrikePlayer player) {
        // Reset
        player.pdcResetAll();

        // Map
        player.pdcSetTabListMap(this.getGameMap());

        // Create tab vars
        player.pdcCreateTabVariable("K-D");
        player.pdcCreateTabVariable("Diff");

        // Create teams
        player.pdcCreateTeam(this.getTeamA());
        player.pdcCreateTeam(this.getTeamB());

        // Disable features
        player.pdcSetFeature(ACFeature.F5, false);
        player.pdcSetFeature(ACFeature.SOUNDBOOST, false);

        // Set team scores
        player.pdcSetTeamScore(this.getTeamA(), this.getTotalScore(this.getTeamA()));
        player.pdcSetTeamScore(this.getTeamB(), this.getTotalScore(this.getTeamB()));

        // populate teams
        pdcRefreshStatsForPlayer(player, this.getTeamA());
        pdcRefreshStatsForPlayer(player, this.getTeamB());
    }

    @Override
    public void respawn(StrikePlayer player, boolean spec)
    {
        if (spec)
            spawnSpectator(player);
        else
            respawnAlive(player);
    }

    /** Spawn player that does not directly participate in game. Could be spectator, coach or dead player. */
    @Override
    public void spawnSpectator(StrikePlayer player)
    {
        // Event
        new PlayerPreSpectateEvent(this, player).trigger();

        // Gamemode
        player.getPlayer().setGameMode(GameMode.SPECTATOR);

        if (getGameSession(player).getStatus() == PlayerStatus.SPECTATING)
        {
            player.getPlayer().teleport(getTempMap().getWorld().getSpawnLocation());
            return;
        }

        if (getGameSession(player).getStatus() == PlayerStatus.COACHING ||
                getGameSession(player).getStatus() == PlayerStatus.PARTICIPATING)
        {
            // Roster of player
            InGameTeamData roster = getPlayerRoster(player);

            // Pick random target to spectate
            final StrikePlayer target = getRandomAliveMember(roster);

            // make sure player will be teleported *somewhere* in new world
            if (player.getWorld() != getTempMap().getWorld() && target == null)
            {
                player.getPlayer().teleport(getTempMap().getWorld().getSpawnLocation());
                return;
            }

            if (target == null)
                return;

            setSpectatorTarget(player, target);
        }
    }

    @Override
    public void respawnAlive(StrikePlayer player)
    {
        // Event
        new PlayerPreRespawnEvent(this, player).trigger();

        // Location
        Location respawn;
        if (getConfig().getValue(ConfigField.RANDOM_SPAWNS).bool()) {
            respawn = getRandomRespawn(player);
        } else {
            respawn = getRespawnPosition(player);
        }

        player.getPlayer().teleport(respawn);

        // Revive
        player.getPlayer().setGameMode(GameMode.ADVENTURE);
        getGameSession(player).setAlive(true);

        // Stuff
        player.heal(this);

        // NPC Trait
        if (player.isNPC()) {
            BotPlayer bot = (BotPlayer) player;
            for (Trait trait : bot.getNPC().getTraits()) {
                if (trait instanceof StrikeTrait strikeTrait) {
                    strikeTrait.onRespawn();
                }
            }
        }
    }

    @LocalEvent
    private void updateNameOnGameModeChange(PlayerPostStatusChangeEvent e) {
        StrikePlayer player = e.getPlayer();

        if (!player.isOnline()) {
            return;
        }

        if (e.getStatus() == PlayerStatus.SPECTATING) {
            // remove score
            player.setScore("");
            player.setDynamicDescription(player.getDefaultTabPrefix() + C.cGray + "[Spectator] ");
        }

        if (e.getStatus() == PlayerStatus.PARTICIPATING ) {
            // remove custom behaviour
            player.setDynamicDescription("");
            player.setScore(formatKD(player));
        }

        if (e.getStatus() == PlayerStatus.COACHING) {
            // remove score
            player.setScore("");
            player.setDynamicDescription(player.getDefaultTabPrefix() + C.cDGray + " [Coach] ");
        }
    }

    private String formatKD(StrikePlayer player) {
        String innerText = (
                player.getKills(this) >= player.getDeaths(this) ?
                        ChatColor.GREEN : ChatColor.RED
        ) + "" + player.getKills(this) + "-" + player.getDeaths(this);

        return ChatColor.DARK_GRAY + " [" + innerText + ChatColor.DARK_GRAY + "]" + ChatColor.RESET;
    }

    @LocalEvent(priority = LocalPriority.POST_HIGH)
    private void updateTabOnScoreUpdate(KdUpdateEvent e) {
        if (!e.getPlayer().isOnline()) {
            return;
        }
        StrikePlayer player = e.getPlayer();
        player.setScore(formatKD(player));
    }

    @LocalEvent
    private void removeScoreOutsideGame(PlayerLeaveGameEvent e) {

        if (!e.getPlayer().isOnline())
            return;

        e.getPlayer().resetTabName();

        if (e.getPlayer().getVersion() > ProtocolVersion.v1_11_1.getVersion()) {
            bossBar.removePlayer(e.getPlayer().getPlayer());
        }

        UniqueSidebar.deregisterBoard(e.getPlayer());

        e.getPlayer().pdcResetAll();

        // really don't like this, I could ignore this even,
        // but one stacktrace less in the console is always good.
        // todo: move player task management to a single place,
        // add scopes to tasks, like round task, game task, player task
        // to manage them automatically.
        var task = e.getPlayer().getRespawnTask(this);
        if (task != null) {
            task.cancel();
            e.getPlayer().setRespawnTask(this, null);
        }
    }

    @LocalEvent(cascade = true)
    private void setDefaultScoreOnConnect(PlayerJoinGameEvent e) {

        int kills = getGameSession(e.getPlayer()).getKills();
        int deaths = getGameSession(e.getPlayer()).getDeaths();

        e.getPlayer().setScore( C.cDGray + " [" + C.cGreen + kills + "-" + deaths + C.cDGray + "]" + ChatColor.RESET );

        pdcSyncClient(e.getPlayer());
        pdcAddPlayerToTab(e.getPlayer());
    }

    @Override
    public int getMaxRounds()
    {
        return  getConfig().getValue(ConfigField.MAX_ROUNDS).val();
    }

    @Override
    public boolean canMove(StrikePlayer player)
    {
        if (isEnded()) {
            return true;
        }

        return currentGameState.is(StateTag.CAN_MOVE) &&
            !(!getConfig().getValue(ConfigField.CAN_DEAD_MOVE).bool() && !getGameSession(player).isAlive());
    }

    @Override
    public boolean canInteractWithItems(StrikePlayer player)
    {
        return getGameSession(player).isAlive() && currentGameState.is(StateTag.CAN_INTERACT);
    }

    @Override
    public DamageManager getDamageManager()
    {
        return damageManager;
    }

    public List<StrikePlayer> getOnline(Iterable<? extends StrikePlayer> offline) {
        return UtilPlayer.getOnline(offline);
    }

    private StrikePlayer getRandomAliveMember(InGameTeamData roster)
    {
        ArrayList<StrikePlayer> alive = new ArrayList<>();
        for (StrikePlayer player : getOnline(roster))
        {
            if (!player.isParticipating(this))
                continue;

            if (getGameSession(player).isAlive())
            {
                alive.add(player);
            }
        }
        if (alive.isEmpty())
            return null;

        return alive.get((int) (alive.size() * Math.random()));
    }

    @Override
    public void broadcast(InGameTeamData roster, String message) {
        for (StrikePlayer player : getOnline(roster)) {
            player.sendMessage(message);
        }
    }

    public BossBar broadcastBossBar(String text, int sDuration, Runnable onDestroy) {

        BossBar bar = Bukkit.createBossBar(text, BarColor.WHITE, BarStyle.SOLID);
        bar.setProgress(1.0);
        bar.setVisible(true);
        bar.setTitle(text);
        for (StrikePlayer player : getOnlinePlayers()) {
            bar.addPlayer(player.getPlayer());
        }
        if (sDuration > -1) {
            new Task(() -> {
                bar.setVisible(false);
                bar.removeAll();
                if (onDestroy != null) {
                    onDestroy.run();
                }
            }, 20 * sDuration).run();
        }
        return bar;
    }

    public BossBar broadcastBossBar(String text, int sDuration) {
        return broadcastBossBar(text, sDuration, null);
    }

    /** here i update all task that depend on game */
    @LocalEvent
    private void updateSessionTasks(TimeEvent e)
    {
        if (e.getReason() != TimeEvent.UpdateReason.TICK)
            return;

        // if gun is dropped it won't be updated,
        // only guns in player inventory are being updated
        for (StrikePlayer player : getAlivePlayers())
        {
            getGameSession(player).getHotbarMessenger().update();

            for (StrikeStack slot : getGameSession(player).getInventory())
            {
                if (slot.isOf(Gun.class))
                {
                    ((Gun) slot.top()).reduceCone();
                }
            }
        }
    }

    @LocalEvent(_native = true)
    private void chat(PlayerChatEvent e) {
        if (e.isCancelled())
            return;

        boolean team = false;
        String raw_message = e.getMessage();

        List<StrikePlayer> receivers;
        if (raw_message.startsWith("#")) {
            // remove hashtag
            receivers = getOnline(getPlayerRoster(e.getPlayer()));
            raw_message = raw_message.substring(1);
            team = true;
        }
        else {
            receivers = getOnline(getOnlinePlayers());
        }

        var session = getGameSession(e.getPlayer());

        String prefix = "";

        if (team) {
            prefix += C.cWhite + C.Bold + "Team " + C.Reset;
        }

        if (!session.isAlive()) {
            prefix += C.cGray + "Dead " + C.Reset;
        }

        String message = prefix + e.getPlayer().getFullChatName(this) + ChatColor.GRAY + " >> " + ChatColor.RESET + e.getPlayer().getRole().getChatMessageColor() + raw_message;

        // broadcast message
        for (StrikePlayer receiver : receivers) {
            receiver.sendMessage(message);
        }
    }

    @LocalEvent(priority = POST)
    private void doSecondUpdate(TimeEvent event)
    {
        if (event.getReason() != TimeEvent.UpdateReason.SECOND)
            return;

        if (!isPaused()) {
            roundTime++;
        }

        // once game is initialized, update state manager every second
//        if (isInitialized && getStateManager().getActiveState().is(StateTag.TICKABLE)) {
//            getStateManager().update();
//        }

        String stateName = currentGameState.getName();
        String mapName = getGameMap().getName();
        String gameName = getMode().getGameName();
        Integer secondsLeft = currentGameStateDuration;
        Integer totalDuration = getConfig().getDuration(currentGameState.getGeneralStage());

        double timeLeft = (double) secondsLeft / totalDuration;

        if (secondsLeft < 0) {
            timeLeft = 1;
        }

        if (getGameState().is(StateTag.INVISIBLE_TIMER)) {
            if ((secondsLeft % 2) == 0) {
                bossBar.setColor(BarColor.YELLOW);
            } else {
                bossBar.setColor(BarColor.RED);
            }
            bossBar.setTitle(mapName + " - " + gameName + " - " + stateName);
            bossBar.setProgress(1);
        } else {
            bossBar.setTitle(mapName + " - " + gameName + " - " + stateName + " " + Time.sFormat(secondsLeft));
            bossBar.setProgress(Math.min(1, Math.max(timeLeft, 0)));

            if (timeLeft > 0.5) {
                bossBar.setColor(BarColor.GREEN);
            }
            else if (timeLeft > 0.25) {
                bossBar.setColor(BarColor.YELLOW);
            }
            else {
                bossBar.setColor(BarColor.RED);
            }
        }
    }

    @LocalEvent
    private void doTickUpdates(TimeEvent event)
    {
        if (event.getReason() != TimeEvent.UpdateReason.TICK)
            return;

        updateGrenadesAndBullets();
        Restore.tickUpdate();

        if (getConfig().getValue(ConfigField.RETROSPECT_HITREG).val() != 0) {
            for (StrikePlayer player : getOnlineDeadOrAliveParticipants()) {
                if (!player.isAlive(this)) {
                    continue;
                }
                LocationRecorder.recordPlayerLocation(player);
            }
        }
    }

    /** Finalize game. Destroys game world and removes players, injects Game End state */
    public void endGame(InGameTeamData winner, InGameTeamData looser) {
        new PreGameEndEvent(this, winner, looser).trigger();

        // clear player inventories
        for (GameSession session : getGameSessions()) {
            session.getInventory().clear();
        }

        if (is(ConfigField.CREATE_NEW_ON_COMPLETION)) {
            Promise<? extends IntentResponse> response = new CreateGameIntentEvent(null, getMode().name(), null).trigger();

            // player to receive message
            final var playersInGame = getOnlinePlayers();

            response.thenSync(
                x -> {
                    var payload = x.getPayload();
                    int newGameId = ((Long) payload.get("game_id")).intValue();

                    TextComponent prefix = new TextComponent(MsgSender.SERVER.form("New game was created with id "));
                    TextComponent gameId = new TextComponent(String.valueOf(newGameId));
                    gameId.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
                    TextComponent click = new TextComponent(". ");

                    TextComponent accept = new TextComponent("Join");
                    accept.setColor(net.md_5.bungee.api.ChatColor.GREEN);
                    accept.setUnderlined(true);
                    accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to join").color(net.md_5.bungee.api.ChatColor.BLUE).create()));
                    accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + newGameId));

                    for (StrikePlayer player : playersInGame) {

                        // so that inventory is visible
                        // todo: Remove spectator mode completely, it's ugly
                        player.getPlayer().setGameMode(GameMode.ADVENTURE);

                        if (player.isOnline()) {
                            player.getPlayer().spigot().sendMessage(prefix, gameId, click, accept);

                            var button = new HotbarButton(Material.EMERALD);
                            button.setTitle(C.cGreen + "Join next game");
                            button.setLore(player.getName());

                            player.addButton(button, 8);

                            button.setOnClick(e -> {
                                var game = MsdmPlugin.getGameServer().getGame(newGameId);
                                if (game == null) {
                                    player.sendMessage(MsgSender.SERVER, C.cRed + "Game not found");
                                    return null;
                                }
                                game.tryJoinPlayer(player, false);
                                player.sendMessage(MsgSender.SERVER, C.cGray + "Connecting to the next game...");

                                // prevent spam clicking
                                player.clearBukkitInventory();
                                return null;
                            });

                            // extremely stupid implementation, but not as stupid as a memory leak
                            // todo: make event for item destruction when inventory is cleared
                            new Task(button::deregister, 20 * 30).run();
                        }
                    }
                    return x;
                }
            );
        }

        ended = true;

        GameStateInstance endedState = new GameStateInstance("Game Ended", GeneralGameStage.GAME_END);

        currentGameState = endedState;
        currentGameStateDuration = 30;

        broadcast(MsgSender.NONE, "", MsgType.CHAT);
        broadcast(MsgSender.GAME, C.cGold + C.Bold + "Game Ended!", MsgType.CHAT);
        broadcast(MsgSender.NONE, "", MsgType.CHAT);

        new Task(()->{
            for (StrikePlayer player : new ArrayList<>(getOnlinePlayers())) {
                tryLeavePlayer(player).thenSync(
                        (x)->{
                            MsdmPlugin.getGameServer().getFallbackServer().join(player);
                            return x;
                        }
                ); // send request to the server for every player to leave
            }
        }, 15 * 20).run();

        new Task(()->{
            for (StrikePlayer player : new ArrayList<>(getOnlinePlayers())) {
                tryLeavePlayer(player); // send request to the server for every player to leave
                MsdmPlugin.getGameServer().getFallbackServer().join(player); // force teleport player somewhere
            }
            destroy();
        }, 30 * 20).run();
    }

    public Integer getPhaseSecondsLeft() {
        return currentGameStateDuration;
    }

    @Override
    public void registerThrownGrenade(Grenade grenade)
    {
        thrownNades.add(grenade);
    }

    @LocalEvent(cascade = true)
    private void checkIfInteractionsAllowed(PlayerUseItemEvent e)
    {
        if (!canInteractWithItems(e.getPlayer()))
            e.setCancelled(true);
    }

    @Override
    public void destroy()
    {
        inProgress = false;

        // this event is handled by lobby
        new GameDestroyEvent(this).trigger();

        // schedule map unload (1 min) when players will be
        // moved to another game for sure
        new Task(_tempMap::unload, 1200).run();
    }

    @Override
    public void restart()
    {
        broadcast(MsgSender.GAME, "Restarting...", MsgType.CHAT);

        // start warm up and stuff
        new GameInitEvent(this).trigger();

        // reset game state
        currentGameState = new GameStateInstance("Warm Up", GeneralGameStage.WARM_UP);

        // destroy player data for every single player who had it (even spectators)
        for (StrikePlayer participant : _everPresentPlayers) {
            getGameSession(participant).reset();
        }

        resetGameInfo();
    }

    @Override
    public boolean canJoin(StrikePlayer player, boolean spectator)
    {
        if (getBlacklist().has(player))
            return false;

        if (getConfig().getValue(ConfigField.WHITELIST).bool() && !getWhitelist().contains(player))
            return false;

        return spectator || getMaxPlayers() > getOnlineDeadOrAliveParticipants().size();
    }

    @Override
    public TempMap getTempMap(){ return _tempMap;}
    @Override
    public GameMap getGameMap(){return _gameMap;}

    /** Players that are  playing in this game right now and are alive */
    @Override
    public List<StrikePlayer> getAlivePlayers()
    {
        return getOnline(getPlayers(PlayerStatus.PARTICIPATING, PlayerState.IN_GAME, true));
    }

    @Override
    public List<StrikePlayer> getAlivePlayers(PlayerGroup<StrikePlayer> group)
    {
        List<StrikePlayer> result = new ArrayList<>();

        for (StrikePlayer p : group) {
            if (getGameSession(p).isAlive()) {
                result.add( p );
            }
        }

        return result;
    }

    /** Players that are in game but waiting to respawn */
    @Override
    public List<StrikePlayer> getDeadPlayers()
    {
        return getOnline(getPlayers(PlayerStatus.PARTICIPATING, PlayerState.IN_GAME, false) );
    }

    @Override
    public List<StrikePlayer> getOnlineSpectators()
    {
        // use game sessions
        return getOnline(getPlayers(PlayerStatus.SPECTATING, PlayerState.IN_GAME, null));
    }

    /** Players that left the game but might be on the server */
    @Override
    public List<StrikePlayer> getAwayPlayers()
    {
        return getPlayers(PlayerState.AWAY);
    }


    /** Players that are right now on the server and in this game, dead as well */
    public List<StrikePlayer> getOnlineDeadOrAliveParticipants() {
        return getOnline(getPlayers(PlayerStatus.PARTICIPATING, PlayerState.IN_GAME, null));
    }

    /** Get list of players that have game session in this game */
    public List<StrikePlayer> getEverParticipated() {
        return getGameSessions().stream().map(GameSession::getPlayer).collect(Collectors.toList());
    }

    /** Players that are currently in the game and online on the server. */
    @Override
    public List<StrikePlayer> getOnlinePlayers() {
        // wrap in getOnline because even IN_GAME players can be offline, since
        // backend changes are not propagated instantly
        return getOnline(getPlayers(null, PlayerState.IN_GAME, null));
    }

    /**
     *  get players with given game state and alive state
     *
     *  @param alive whether to get alive dead or both players
     *  @param status desired player state
     *
     *  @return player group of either online of offline with specific state. Copy, does not reference original array. */
    private List<StrikePlayer> getPlayers(@Nullable PlayerStatus status, @Nullable PlayerState state, @Nullable Boolean alive)
    {
        List<StrikePlayer> p = new ArrayList<>();

        for (GameSession session : getGameSessions())
        {
            if (status != null && session.getStatus() != status) {
                continue;
            }

            if (state != null && session.getState() != state) {
                continue;
            }

            if (alive != null && session.isAlive() != alive) {
                continue;
            }

            p.add(session.getPlayer());
        }
        return p;
    }
    private List<StrikePlayer> getPlayers(PlayerState state, @Nullable Boolean alive) {
        return getPlayers(null, state, alive);
    }

    private List<StrikePlayer> getPlayers(PlayerStatus status)
    {
        return getPlayers(status, null, null);
    }

    private List<StrikePlayer> getPlayers(PlayerState state) {
        return getPlayers(state, null);
    }

    @Override
    public void setRandomSpectateTarget(StrikePlayer spectator)
    {
        spectator.getPlayer().setGameMode(GameMode.SPECTATOR);

        InGameTeamData roster = getPlayerRoster(spectator);

        // spectators don't have a roster
        if (roster == null) {
            return;
        }

        StrikePlayer randomTarget = getRandomAliveMember(roster);

        if(randomTarget == null)
            return;

        setSpectatorTarget(spectator, randomTarget);
    }

    @Override
    public List<StrikePlayer> getCoaches() {
        return getOnline(getPlayers(PlayerStatus.COACHING));
    }

    @Override
    public int getRoundTime()
    {
        return roundTime;
    }


    @Override
    public void deregisterDroppedItem(Entity ent)
    {
        droppedItems.remove(ent);
        ent.remove();
    }

    public void deregisterDroppedItem(StrikeItem item)
    {
        Entity ent = droppedItems.inverse().remove(item);
        if (ent != null)
            ent.remove();
    }


    @Override
    public boolean away(StrikePlayer player)
    {
        return getGameSession(player)==null || getGameSession(player).getState() == PlayerState.AWAY;
    }

    HashMap<Location, Long> incendiary = new HashMap<>();
    HashMap<Block, FireBlockData> fireBlocks = new HashMap<>();
    HashMap<Location, Long> smokes = new HashMap<>();


    @Override
    public void registerIncendiary(Location loc, long l)
    {
        incendiary.put(loc, l);
    }

    @Override
    public void registerFireBlock(Block block, Grenade source, Long expires) {
        fireBlocks.put(block, new FireBlockData(source, expires));
    }

    @Override
    public Grenade getFireStarterAt(@Nonnull Block block) {
        FireBlockData blockData = fireBlocks.get(block);

        if (blockData == null)
            return null;

        if (blockData.expired()) {
            fireBlocks.remove(block);
            return null;
        }

        return blockData.source;
    }

    @Override
    public void registerSmoke(Location loc, long duration)
    {
        smokes.put(loc, duration);
    }


    @Override
    public void pickUpFromGround(StrikePlayer player, StrikeItem item, Item ent)
    {
        item.giveToPlayer(this, player, false);

        // as a side effect only items with fixed slots will trigger onFocus event
        if (item.getSlot() != null && item.getSlot() == player.getPlayer().getInventory().getHeldItemSlot()){
            new ItemFocusEvent(item, this, player).trigger();
        }

        // deregister
        InvulnerableItem.pickUp(ent);
        deregisterDroppedItem(ent);
    }

    @Override
    public void deleteDropped()
    {
        // remove item entities
        for (Entity entity : droppedItems.keySet()) {
            entity.remove();
        }
        droppedItems.clear();
    }

    @Override
    public void deleteNades()
    {
        // remove item entities
        for (Grenade grenade : thrownNades) {
            grenade.removeEntity();
        }
        thrownNades.clear();
    }

    @Override
    public StrikeItem getItem(Item item)
    {
        return droppedItems.get(item);
    }

    @Override
    public void registerDroppedItem(Item ent, StrikeItem strikeItem)
    {
        droppedItems.put(ent, strikeItem);

    }

    public Entity findDroppedItem(StrikeItem item)
    {
        return droppedItems.inverse().get(item);
    }


    @Override
    public void breakGlass(Block block)
    {
        Restore.MakeRevertibleChange(block, Material.AIR, 9999999);

        ArrayList<Block> blocks = new ArrayList<>();

        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (UtilBlock.isGlassPane(block.getRelative(x, y, z)))
                        blocks.add(block.getRelative(x, y, z));
                }
            }

        if (blocks.size() <= 4) {
            for (Block b : blocks) {
                Restore.MakeRevertibleChange(b, Material.AIR, 9999999);
            }
        } else {
            secondaryGlassBreakage(block, new HashSet<>());
        }

        getSoundManager().breakGlass().at(block.getLocation()).play();
    }

    private void secondaryGlassBreakage(Block center, HashSet<Block> alreadyChecked) {

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    final Block b = center.getRelative(x, y, z);
                    if (alreadyChecked.contains(center.getRelative(x, y, z))) {
                        continue;
                    }

                    if (UtilBlock.isGlassPane(b)) {
                        if (Math.random() > 0.6) {
                            Restore.MakeRevertibleChange(b, Material.AIR, 9999999);
                            alreadyChecked.add(b);
                            new Task(() -> secondaryGlassBreakage(b, alreadyChecked), 1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public StrikePlayer getParticipant(String ign)
    {
        for (StrikePlayer player : this.getOnlineDeadOrAliveParticipants())
        {
            if (player.getName().equals(ign))
                return player;
        }
        return null;
    }

    @Override
    public boolean isEnded()
    {
        return ended;
    }

    @LocalEvent
    private void setupCoach(PlayerPostStatusChangeEvent e)
    {
        // TODO: when implementing coaching have to track status changes from backend somehow
        if (e.getStatus() == PlayerStatus.COACHING && e.getPlayer().isOnline())
        {
            setRandomSpectateTarget(e.getPlayer());
        }
    }

    @Override
    public PlayerGroup<StrikePlayer> getBlacklist() {
        return new PlayerView<>(super.getBlacklistedPlayers());
    }

    @Override
    public GameSession getGameSession(StrikePlayer player)
    {
        return sessions.get(player);
    }

    @LocalEvent(priority = PRE)
    private void wallbangLimit(BulletWallbangEvent e) {

        // bullet travelled more than gun can penetrate
        if (e.getBullet().getWallbangPenalty() == 0) {
//            e.getBullet().markStopped();
            e.setCancelled(true);
        }
    }

    @Override
    public void setPlayerReady(StrikePlayer player)
    {
        new PlayerReadyEvent(this, player).trigger();

        readyList.put(player, true);

        // Trigger event
        if (everyoneReady())
            new ParticipantsReadyEvent(this).trigger();

    }

    @Override
    public boolean readyStateRequiredByConfig()
    {
        return getConfig().getValue(ConfigField.REQUIRE_READY).bool();
    }

    @Override
    public boolean isPlayerReady(StrikePlayer player)
    {
        return readyList.get(player);
    }

    @Override
    public boolean everyoneReady()
    {
        for (Boolean ready : readyList.values()) {
            if (!ready)
                return false;
        }

        return true;
    }

    @Override
    public ArrayList<StrikePlayer> getNotReadyPlayers()
    {
        ArrayList<StrikePlayer> notReady = new ArrayList<>();
        for (StrikePlayer player : readyList.keySet())
        {
            if (getGameSession(player).getStatus() != PlayerStatus.PARTICIPATING)
                continue;

            if (!readyList.get(player))
            {
                notReady.add(player);
            }
        }
        return notReady;
    }

    @Override
    public boolean canStart()
    {
        if ( getConfig().getValue(ConfigField.REQUIRE_READY).bool() && !everyoneReady())
            return false;

        return hasEnoughPlayers();
    }


    @LocalEvent // Arrow Bullet default effects
    public void instantBulletHit(BulletStopEvent event)
    {
        /* Play tracer from bullet origin to location where it stopped */
        if (!event.getBullet().isInstant())
            return;

        Bullet bullet = event.getBullet();

        //Bullet Whiz Sound
        // instantBulletWhizz(arrow.getLocation(), bullet);
        bullet.playTracer(event.getBullet().getLocation());
    }

    @LocalEvent(cascade = true) // common Bullet hit effects
    public void commonHitEffects(final BulletHitEvent event) {

        if (event.getGame().is(ConfigField.DEBUG_HITREG)) {
            var bulletLog = event.getGame().getHitRegLog().getBulletLog(event.getBullet());
            if (bulletLog != null) {
                bulletLog.bulletHit(
                    event.getHitLocation(),
                    event.getHitPlayer(),
                    System.currentTimeMillis()
                );
            }
        }

        //Particle
        UtilParticle.PlaySpark(event.getPreLocation());

        //Hit Block Sound
        getSoundManager().blockHit().at(event.getHitLocation()).play();

        Block block = event.getHitLocation().getBlock();

        if (UtilBlock.isGlassPane(block))
            event.getGame().breakGlass(block);

        //Block Particle
        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
    }

    @LocalEvent
    private void handleBulletHit(BulletHitPlayerEvent event) {
        if (event.getHitPlayer() == null) {
            return;
        }

        StrikePlayer attacker = event.getBullet().getShooter();

        if (event.getBullet().isInstant()) {
            attacker.playSound(Sound.ENTITY_ARROW_HIT_PLAYER, 100, 0);
        }

        damageManager.handleGunDamage(
                event.getGame(),
                event.getHitPlayer(),
                attacker,
                event.getDamageeLocation(),
                event.getBullet(),
                event.getBullet().getGun(),
                event.getHitArea()
        );
    }

    @LocalEvent
    private void hitEntityEffects(BulletHitEvent e) {
        if (e.getHitEntity() != null) {
            if (e.getHitEntity() instanceof Chicken) {
                e.getHitEntity().remove();
                UtilEffect.chickenExplosion(e.getHitEntity().getLocation());
            }
        }
    }

    @Override
    public boolean hasEnoughPlayers()
    {
        return this.getOnlineDeadOrAliveParticipants().size() >= getMinPlayers();
    }

    @Override
    public boolean isInProgress()
    {
        return inProgress;
    }

    @Override
    public DamageLog getDamageLog() {
        return damageLog;
    }

    @Override
    public RestoreManger getRestore() {
        return Restore;
    }

    @Override
    public StrikePlayer getPlayer(String ign) {
        for (StrikePlayer player : getOnlineDeadOrAliveParticipants()) {
            if (player.getName().equalsIgnoreCase(ign)) {
                return player;
            }
        }
        return null;
    }

    @Override
    public InGameTeamData[] getRosters()
    {
        return new InGameTeamData[]{getTeamA(), getTeamB()};
    }

    public InGameTeamData getRoster(StrikeTeam team) {
        if (getTeamA().getTeam() == team) {
            return getTeamA();
        }
        return getTeamB();
    }

    public List<GameSession> getGameSessions() {
        return new ArrayList<>(sessions.values());
    }

    public HitRegLog getHitRegLog() {
        return _hitRegLog;
    }

    @LocalEvent
    private void onGrenadeBought(PlayerShopEvent e) {
        // record amount of bought grenades
        if (e.getBoughtItem() instanceof Grenade) {
            getDataRegistry().increment(e.getPlayer(), DataScope.ROUND_LIFE, DataKey.GRENADES_BOUGHT, 0);
        }
    }

    /** Toggle bounding box visibility */
    @LocalEvent
    private void onConfigChange(ConfigFieldChangeEvent e) {
        if (e.getField() == ConfigField.HIGHLIGHT_BOUNDING_BOXES) {
            if (e.getNewValue() == 1) {
               if (highlightBoxesTask != null) {
                   return;
               }
                highlightBoxesTask = new Task(
                    () -> WorldTools.showBoundingBoxes(getTempMap()),
                    1, 10
                ).run();
            } else if (highlightBoxesTask != null) {
                highlightBoxesTask.cancel();
                highlightBoxesTask = null;
            }
        }
        if (e.getField() == ConfigField.SHOW_SPAWNS) {
            if (e.getNewValue() == 1) {
                WorldTools.showRespawns(getTempMap());
            } else {
                WorldTools.hideRespawns(getTempMap());
            }
        }
    }

    public void setLeftDuration(int duration) {
        this.currentGameStateDuration = duration;
    }

    public @Nullable Scoreboard getPlayerScoreboard(StrikePlayer strikePlayer) {
        if (getGameSession(strikePlayer).getRoster().getTeam() == getTeamA().getTeam()) {
            return teamAScoreboard;
        }
        if (getGameSession(strikePlayer).getRoster().getTeam() == getTeamB().getTeam()) {
            return teamBScoreboard;
        }
        return null;
    }
}
