package obfuscate.game.player;

import net.md_5.bungee.api.chat.BaseComponent;
import obfuscate.MsdmPlugin;
import obfuscate.event.LocalEvent;
import obfuscate.event.custom.network.PlayerDataReceivedEvent;
import obfuscate.event.custom.player.*;
import obfuscate.event.custom.team.PlayerJoinRosterEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.*;
import obfuscate.game.damage.NamedDamageSource;
import obfuscate.game.debug.ViewRecorder;
import obfuscate.game.sound.Radio;
import obfuscate.gamemode.Competitive;
import obfuscate.mechanic.item.StrikeStack;
import obfuscate.mechanic.item.guns.StrikeItemType;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.message.MsgSender;
import obfuscate.podcrash.ACFeature;
import obfuscate.podcrash.PodcrashIntegration;
import obfuscate.ui.component.HotbarButton;
import obfuscate.util.chat.Message;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.permission.Permission;
import obfuscate.team.InGameTeamData;
import obfuscate.team.StrikeTeam;
import obfuscate.util.chat.C;
import obfuscate.util.hotbar.HotbarMessager;
import obfuscate.util.UtilTeam;
import obfuscate.util.time.Task;
import obfuscate.world.GameMap;
import com.comphenix.packetwrapper.AbstractPacket;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

import static org.bukkit.craftbukkit.libs.jline.internal.Preconditions.checkNotNull;

public class StrikePlayer extends OfflineStrikePlayer implements NamedDamageSource
{

    // player-specific data that stays with player
    // and does not care about game

    private boolean disguised;
    private Integer disguiseLoop = null;
    private boolean opBypass = false;
    private String _actualName;

    private boolean clientUsed = false;

    // store last 3 seconds of player movement
    private ArrayList<PlayerMovement> _movementHistory = new ArrayList<>();

    private long _lastRightClick = 0;

    private HashMap<Object, HashSet<Permission>> temporaryPermissions = new HashMap<>();


    // this is required for automatically created offline players
    public StrikePlayer() {

    }

    public static @Nullable StrikePlayer findByName(String name) {
        MsdmPlugin.highlight("Find player by name: " + name);
        var bPlayer = Bukkit.getPlayer(name);

        if (bPlayer == null) {
            MsdmPlugin.highlight("Player not found");
            return null;
        }

        MsdmPlugin.highlight("Player found: " + bPlayer);
        return StrikePlayer.getOrCreate(bPlayer);
    }

    public void setLastRightClick(long time) {
        _lastRightClick = time;
    }

    public long sinceLastRightClick() {
        return System.currentTimeMillis() - _lastRightClick;
    }

    private StrikePlayer(Player player) {
        // we want to make wrapper methods accessible right after initialization
        this.uuid = player.getUniqueId();
    }

    public boolean isNPC()
    {
        return false;
    }

    public boolean isDebugging()
    {
        return opBypass;
    }


    private static final HashMap<UUID, StrikePlayer> strikePlayers = new HashMap<>();

    private String visibleName;
    private org.bukkit.util.Vector velocity = new org.bukkit.util.Vector(0,0,0);

    // move time
    public double _lastMove;
    // since last move
    public long _lastMoveTime;

    // additional info for player displaying.
    private String score = "";
    private String dynamicNameDescription = "";

    public boolean isAlive(Game game)
    {
        return getActiveSession(game).isAlive();
    }

    /* scoreboard */
    public Integer getKills(Game game)
    {
        return game.getGameSession(this).getKills();
    }

    public Integer getDeaths(Game game)
    {
        return game.getGameSession(this).getDeaths();
    }

    public void clearBukkitInventory() {
        getPlayer().getInventory().clear();
    }

    public String getDefaultTabName() {

        if (getRole() == null) {
            return this.getName();
        }

        return getRole().getChatPrefix() + getTabNameColor(null) + this.getName() + getRole().getChatSuffix();
    }

    public String getFullTabName() {
        return (isBmsClientUsed() ? C.cGreen : C.cRed) + "⬤ " + C.cWhite + getElo() + " Elo " + dynamicNameDescription + getDefaultTabName() + score;
    }

    /** Remove suffixes and prefixes in game */
    public String getInGameTabName(Game game) {
        return (isBmsClientUsed() ? C.cGreen : C.cRed) + "⬤ " + C.cWhite + getElo() + " Elo " + dynamicNameDescription + getTabNameColor(game) + getName() + score;
    }

    public String getInGameChatName() {
        return "";
    }

    public void setScore(String score) {
        this.score = score;
        updateTabName(getGame());
    }

    public void resetTabName() {
        setScore("");
        setDynamicDescription("");
    }

    public void setDynamicDescription(String descr) {
        dynamicNameDescription = descr;
        updateTabName(getGame());
    }

    public String getDefaultTabPrefix() {
        String prefix = "";
        if (getRole().getTabPrefix() != null)
            prefix = getRole().getTabPrefix() + " ";
        return prefix;
    }

    public String getTabNameColor(Game game) {
        String tabNameColor = "";
        if (getRole().getTabColor() != null)
            tabNameColor = getRole().getTabColor();

        if (getRole().getTeamOverrideColor() && game != null) {
            InGameTeamData roster = game.getPlayerRoster(this);

            if (roster != null && !isSpectating(game)) {
                return roster.getTeam().getColor();
            }
        }

        return tabNameColor;
    }

    public void updateTabName(Game game)
    {
        if (game != null) {
            getPlayer().setPlayerListName( getInGameTabName(game) );
            return;
        }
        getPlayer().setPlayerListName( getFullTabName() );
    }

    public boolean hasPermission(Permission perm)
    {
        if (getPlayer().isOp())
            return true;

        if (getRole() == null) {
            MsdmPlugin.highlight("Player " + getName() + " has no role assigned!");
            return false;
        }

        return getRole().hasPermission(perm);
    }

    public boolean hasPermissionIn(Permission perm, Object scope) {
        if (hasPermission(perm)) {
            return true;
        }
        Set<Permission> perms = temporaryPermissions.get(scope);
        if (perms == null) {
            return false;
        }
        return perms.contains(perm);
    }

    public String getFullChatName(@Nullable Game game)
    {
        String result = "";
        if (getRole().getChatPrefix()!=null)
            result += getRole().getChatPrefix();

        if (game != null && getRole().getTeamOverrideColor() && game.getPlayerRoster(this) != null) {
            result += game.getPlayerRoster(this).getTeam().getColor();
        } else {
            result += getRole().getChatColor();
        }

        result += this.getName();

        if (getRole().getChatSuffix() != null)
            result += getRole().getChatSuffix();

        return result + C.Reset;
    }

    public String getName()
    {
        if (visibleName == null)
            return getPlayer().getName();

        return visibleName;
    }

    private ItemStack colorItem(Game game, ItemStack item)
    {
        Color color = game.getPlayerRoster(this).getTeam() == StrikeTeam.CT ?
                Color.fromRGB(125, 200, 255) :
                Color.fromRGB(255, 75, 75);

        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    /** give default equipment to player */
    public void equip(Game game)
    {
        ItemStack helm = colorItem(game, new ItemStack(Material.LEATHER_HELMET));
        ItemStack chest = colorItem(game, new ItemStack(Material.LEATHER_CHESTPLATE));
        ItemStack legs = colorItem(game, new ItemStack(Material.LEATHER_LEGGINGS));
        ItemStack boots = colorItem(game, new ItemStack(Material.LEATHER_BOOTS));

        if (game.getGameSession(this).getInventory().hasHelmet()) {
            getPlayer().getInventory().setHelmet(helm);
        }
        else {
            getPlayer().getInventory().setHelmet(null);
        }

        getPlayer().getInventory().setChestplate(chest);
        getPlayer().getInventory().setLeggings(legs);
        getPlayer().getInventory().setBoots(boots);
    }

    public @Nullable Player getPlayer()
    {
        if (CitizensAPI.getNPCRegistry().getByUniqueId(getUuid()) != null) {
            throw new RuntimeException("Is NPC!");
        }

        return Bukkit.getPlayer(this.uuid);
    }

    public void setVisibleName(String visibleName)
    {
        this.visibleName = visibleName;
        new Task(() -> new TabNameUpdateEvent("dummy do not use", this).trigger(), 0).run();
        // update all tabs?

    }

    @LocalEvent
    public void onTeamJoin(PlayerRosterChangeEvent e)
    {
        MsdmPlugin.highlight("PlayerRosterChangeEvent " + e.getPlayer().getName() + " - " + e.getNewTeam().getNiceName());
        updateTeam(e.getGame(), e.getNewTeam());
    }

    @LocalEvent(cascade = true)
    public void onRosterJoin(PlayerJoinRosterEvent e)
    {
        var roster = e.getRoster();

        // shortcut for spectators
        if (roster == null) {
            return;
        }

        MsdmPlugin.important("PlayerJoinRosterEvent " + e.getPlayer().getName() + " - " + roster.getTeam().getNiceName());

        MsdmPlugin.highlight("Player joined game. Can see team names: " + e.getGame().getConfig().getValue(ConfigField.CAN_SEE_TEAM_NAMES).bool());
        updateTeam(e.getGame(), roster);

    }

    public Scoreboard getScoreboard(IGame game)
    {
        return game.getPlayerScoreboard(this);
    }

    @LocalEvent
    private void playerJoinedServer(PlayerJoinServerEvent e) {
        Player bPlayer = Bukkit.getPlayer(getUuid());
        setVisibleName(bPlayer.getName());
        _actualName = bPlayer.getName();

        // update player count in footer
        MsdmPlugin.getInstance().updateHeaderFooter();

        // remove joined player from game tabs
        this.loadResources();

        // Tab
        this.updateTabName(null);

        // Lobby
        MsdmPlugin.getGameServer().getFallbackServer().join(this);
        MsdmPlugin.sendGreetMessage(e.getPlayer());
    }

    /**
     * @see StrikePlayer#updateTeam(Game, StrikeTeam)
     * */
    private void updateTeam(Game game, @Nullable InGameTeamData roster)
    {
        if (roster == null) {
            MsdmPlugin.important("updateTeam " + getName() + " - NULL");
            updateTeam(game, (StrikeTeam) null);
            return;
        }
        MsdmPlugin.important("updateTeam " + getName() + " - " + roster.getTeam().getNiceName());
        updateTeam(game, roster.getTeam());

    }

    /**
     * Basic way to update player's bukkit team
     * to affect tab list and name tag
     * */
    public void updateTeam(Game game, StrikeTeam team)
    {
        if (team != null)
        {
            equip(game);
            updateTabName(game);
            // join bukkit team to set name tag visibility
            UtilTeam.join(
                    this,
                    getScoreboard(game),
                    team,
                    team.name(),
                    game.getConfig().getValue(ConfigField.CAN_SEE_TEAM_NAMES).bool()
            );
        } else {
            UtilTeam.leave(this, getScoreboard(game));
        }
    }


    public @NotNull StrikeStack getHeldSlot(Game game)
    {
        int slot = getHeldSlot();
        return getActiveSession(game).getInventory().getStack(slot);
    }

    public boolean isSpectating(Game game)
    {
        return getActiveSession(game).getStatus() == PlayerStatus.SPECTATING;
    }

    public Task getRespawnTask(Game game)
    {
        return getActiveSession(game).getRespawnTask();
    }

    public Task setRespawnTask(Game game, Task task)
    {
        getActiveSession(game).setRespawnTask(task);
        return task;
    }

    public double getLastMoveDistance(Game Game)
    {
        return _lastMove;
    }

    public long getLastMoveTime(Game Game)
    {
        return _lastMoveTime;
    }

    public void addToLastMoveTime(Game Game, long time)
    {
        _lastMove += time;
    }

    public long sinceMove()
    {
        return System.currentTimeMillis() - _lastMoveTime;
    }


    public boolean canSurviveDamage(double damage)
    {
        return getPlayer().getHealth() - damage > 0;
    }

    public boolean immuneToKnife(Game Game)
    {
        return false;
    }

    /** Get the game player participates in.
     * use only to get link to game from events where only bukkit player is available */
    @Nullable
    public Game getGame()
    {
        return MsdmPlugin.getGameServer().getGame(this);
    }

    @Deprecated
    public GameSession getActiveSession(Game game)
    {
        return game.getGameSession(this);
    }

    public void setInvulnerable(Game game, int for_ticks)
    {
        if (getActiveSession(game).isInvulnerable())
        {
            getActiveSession(game).getInvulnerableTask().cancel();
        }

        getActiveSession(game).setInvulnerable(true);
        Task task = new Task( ()-> getActiveSession(game).setInvulnerable(false), for_ticks);
        getActiveSession(game).setInvulnerableTask(task);
        task.run();
    }

    public void playSound(Radio sound)
    {
        getPlayer().playSound(getLocation(), sound.getSound(), 100, 1);
    }

    public void playSound(Sound sound) {
        getPlayer().playSound(getLocation(), sound, 100, 1);
    }

    public void playSound(Sound sound, float vol, float pitch) {
        getPlayer().playSound(getLocation(), sound, vol, pitch);
    }

    public boolean hasEffect(PotionEffectType effect) {
        return getPlayer().hasPotionEffect(effect);
    }

    public void removeEffect(PotionEffectType e) {
        getPlayer().removePotionEffect(e);
    }

    public void addPotionEffect(PotionEffectType effect, int duration, int amplifier)
    {
        getPlayer().addPotionEffect(new PotionEffect(effect, duration, amplifier, false, false));
    }

    public void makeSound(Radio sound, float vol, float pitch)
    {
        makeSound(sound.getSound(), vol, pitch);
    }

    public void makeSound(Sound sound, float vol, float pitch)
    {
        getPlayer().getWorld().playSound(getLocation(), sound, vol, pitch);
    }

    public int getHeldSlot()
    {
        return getPlayer().getInventory().getHeldItemSlot();
    }

    public boolean isInvulnerable(Game game)
    {
        return getActiveSession(game).isInvulnerable();
    }

    public void setMoveInfo(Location from, Location to)
    {
        updateDirection(from, to);
        _lastMoveTime = System.currentTimeMillis();
        _lastMove = getVelocity().length();
        _movementHistory.add(new PlayerMovement(from, to, _lastMoveTime));

        // delete movement history older than 3 seconds
        _movementHistory.removeIf(x-> x.getTimestamp() < _lastMoveTime - 3000);
    }

    public Vector getAverageMove(int ms) {
        // find resulting movement in last {ms} milliseconds
        Vector result = new Vector(0,0,0);
        for (PlayerMovement movement : _movementHistory) {
            if (movement.getTimestamp() > System.currentTimeMillis() - ms) {
                result.add(new Vector(movement.getDx(), movement.getDy(), movement.getDz()));
            }
        }
        return result;
    }

//    public Vector predictLook(double pitch, double yaw) {
//
//        double pitchChange = 0;
//        double yawChange = 0;
//
//        if (!_movementHistory.isEmpty()) {
//            PlayerMovement move = _movementHistory.get(_movementHistory.size() - 1);
//            if (move.getTimestamp() >= System.currentTimeMillis() - 50) {
//                pitchChange = move.getPitchChange();
//                yawChange = move.getYawChange();
//            }
//        }
//
//        Vector vector = new Vector();
//        double rotX = yaw + yawChange;
//        double rotY = pitch + pitchChange;
//        vector.setY(-Math.sin(Math.toRadians(rotY)));
//        double xz = Math.cos(Math.toRadians(rotY));
//        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
//        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));
//
//        return vector;
//    }
    public Vector predictLook(int ticks) {
        var historyContainer = ViewRecorder.getInstance().getHistory(this);

        historyContainer.record();
        ArrayList<double[]> history = historyContainer.getViewHistory(10);
        int last = history.size() - 1;
        double[] lastView = history.get(last);
        double[] prevView = history.get(last - 1);

        double dx = lastView[0] - prevView[0];
        double dy = lastView[1] - prevView[1];
        double dz = lastView[2] - prevView[2];

        Vector look = this.getEyeLocation().getDirection();

        look.add(new Vector(dx, dy, dz).multiply(ticks));
        return look;
    }

    @Deprecated
    public Vector getVelocity() {
        return velocity;
    }
    public Vector getAcceleration() {
        return getPlayer().getVelocity();
    }

    private void updateDirection(Location oldLocation, Location newLocation)
    {
        velocity = newLocation.toVector().subtract(oldLocation.toVector());
    }

    public void damage(double damage, Game game)
    {
        for (Player player : getPlayer().getWorld().getPlayers())
        {
            // damage animation
            CraftPlayer e = (CraftPlayer) getPlayer();
            net.minecraft.world.entity.Entity en = e.getHandle();
            PacketPlayOutAnimation pack = new PacketPlayOutAnimation(en, 1);
            ((CraftPlayer) player).getHandle().b.sendPacket(pack);
        }
        double health = getPlayer().getHealth();
        double newHealth = health - damage;
        setHealth(newHealth, game);
    }

    public EntityPlayer getHandle()
    {
        return ((CraftPlayer) getPlayer()).getHandle();
    }


    /* scope */
    public void addScopeEffect()
    {
        getPlayer().getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 9999, 2, false, false));
    }

    public void removeScopeEffect()
    {
//        getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
        getPlayer().removePotionEffect(PotionEffectType.SLOW);
        getPlayer().getWorld().playSound(getPlayer().getEyeLocation(), Sound.ENTITY_GHAST_DEATH, 0.8f, 1f);
    }

    public boolean scoped(Game game)
    {
        return getActiveSession(game).isScoped();
    }

    public void shoot(Game game)
    {
        // player handle right click
        Gun gun = getGunInHand(game);
        if (gun == null) {
            return;
        }
        gun.shoot(this, game);
    }

    public void partialHeal(float amount, IGame game)
    {
        double health = getPlayer().getHealth();
        setHealth(Math.min(getPlayer().getMaxHealth(),  health + getPlayer().getMaxHealth() * amount), game);
    }

    public void heal(IGame game)
    {
        setHealth(20, game);
    }

    public void setHealth(double health, IGame game) {

        getPlayer().setHealth(health);

        var scoreboard = getScoreboard(game);
        var objective = scoreboard.getObjective("hp");
        Score score = objective.getScore(this.getPlayer());
        score.setScore((int) Math.round(health * 5));
        getPlayer().setScoreboard(scoreboard);
    }

    public static BotPlayer getOrCreate(NPC npc)
    {
        UUID uuid = npc.getEntity().getUniqueId();
        MsdmPlugin.important("getOrCreate NPC: " + uuid);

        if (!strikePlayers.containsKey(uuid)) {
            BotPlayer player = new BotPlayer(npc);
            MsdmPlugin.getBackend().getBotPlayerId(uuid).thenSync(
                    x -> {
                        player.setId(new ObjectId(x, "Player", null));
                        return null;
                    }
            );
            strikePlayers.put(uuid, player);
        }
        MsdmPlugin.important("NPC Created: " + strikePlayers.get(uuid));
        return (BotPlayer) strikePlayers.get(uuid);
    }

    public static StrikePlayer getOrCreate(Player player)
    {
//        MsdmPlugin.info("GetOrCreate Player: " + player.getUniqueId());
        // if not create one
        if (strikePlayers.containsKey(player.getUniqueId()))
        {
            return strikePlayers.get(player.getUniqueId());
        }

        // check if we have player in model registry
        for (SyncableObject item : MsdmPlugin.getBackend().trackedModels.values()) {
            if (item instanceof StrikePlayer savedPlayer) {
                // for bot players uuid is NULL causing problems
                if (savedPlayer.getUuid().equals(player.getUniqueId())) {
                    // player was found in models storage, save to cache
                    strikePlayers.put(player.getUniqueId(), savedPlayer);
                    return savedPlayer;
                }
            }
        }

        StrikePlayer p;

//        if (CitizensAPI.getNPCRegistry().isNPC(player))
//        {
//            p = new BotPlayer(CitizensAPI.getNPCRegistry().getNPC(player));
//        }
//        else
//        {
        MsdmPlugin.logger().info("[GetOrCreate player] nothing found call constructor");
        p = new StrikePlayer(player);
        MsdmPlugin.getBackend().getPlayerId(player.getUniqueId()).thenSync(
                x -> {
                    p.setId(new ObjectId(
                            x,
                            "Player",
                            null
                    ));
                    return null;
                }
        );
//        }
        strikePlayers.put(player.getUniqueId(), p);

        return p;
    }

    public static StrikePlayer getOrCreate(UUID uuid) {

        checkNotNull(uuid);

        Player bukkitPLayer = Bukkit.getPlayer(uuid);

        if (bukkitPLayer == null) {
            MsdmPlugin.logger().log(Level.SEVERE, "PLayer with UUID " + uuid + "is not online!");
        }

        return getOrCreate(bukkitPLayer);
    }

    public static ArrayList<StrikePlayer> getInWorld(World world) {
        ArrayList<StrikePlayer> players = new ArrayList<>();
        for (StrikePlayer player : strikePlayers.values()) {
            if (player.getWorld() == world) {
                players.add(player);
            }
        }
        return players;
    }

    public GameInventory getInventory(Game game)
    {
        return getActiveSession(game).getInventory();
    }

    public boolean hasItem(Game game, int i)
    {
        if (getInventory(game) == null)
            return false;

        return getInventory(game).hasItem(i);
    }

    public World getWorld() {
        return getLocation().getWorld();
    }

    public static Iterable<StrikePlayer> getAll()
    {
        return strikePlayers.values();
    }

    public static Iterable<StrikePlayer> getOnline() {
        ArrayList<StrikePlayer> players = new ArrayList<>();
        for (StrikePlayer player : getAll()) {
            if (player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    public Location getLocation()
    {
        return getPlayer().getLocation();
    }

    public Location getEyeLocation()
    {
        return getPlayer().getEyeLocation();
    }

    public Projectile launchProjectile(Class<?extends Projectile> arrowClass)
    {
        return getPlayer().launchProjectile(arrowClass);
    }

    public void animateHand()
    {
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(getHandle(), 0);
        for (Player player : getPlayer().getWorld().getPlayers())
        {
            ((CraftPlayer) player).getHandle().b.sendPacket(packet);
        }
    }

    public void wipeKD(Game game) {
        getActiveSession(game).resetKD();
    }


    public PlayerState getState(Game game)
    {
        return getActiveSession(game).getState();
    }

    public void setInvulnerable(Game game, boolean b)
    {
        getActiveSession(game).setInvulnerability(b);
    }

    public boolean isParticipating(@Nullable Game game)
    {
        if (game == null)
            return false;
        return game.getGameSession(this).getStatus() == PlayerStatus.PARTICIPATING;
    }



    private String genFunName(String src)
    {
        Random rand = new Random();
        int left = rand.nextInt(src.length() - 1 );
        String newName = src.substring(0,left)+'?'+src.substring(left+1);
        return newName;
    }

    public void toggleDebug()
    {
        opBypass = !opBypass;
    }

    // todo add event
    public void onTeamLeave(Game game)
    {
        // called before actually leaving so current session is still there
        UtilTeam.leave(this, getScoreboard(game));

        // remove armor
        getPlayer().getInventory().setArmorContents(new ItemStack[4]);
    }

    public void sendTitle(String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        getPlayer().sendTitle(title, subtitle, fadeInTime, showTime, fadeOutTime);
    }

    @LocalEvent(cascade = true)
    private void updateTabNameOnEvent(TabNameUpdateEvent e) {
        updateTabName(getGame());
    }

    public void reloadAll(IGame game) {
        game.getGameSession(this).getInventory().reloadGuns(game);
    }

    public void sendTitle(String title, String subtitle, Integer fadeIn, Integer stay, Integer fadeOut) {
        getPlayer().sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void sendTitle(String title, Integer fadeIn, Integer stay, Integer fadeOut) {
        sendTitle(title, "", fadeIn, stay, fadeOut);
    }

    public void setBossbar(String title, Integer percent) {

    }

    public void sendTitle(String titleS, int fadeIn, int stay, int fadeOut)
    {
        getPlayer().sendTitle(titleS, "", fadeIn, stay, fadeOut);
    }

    public void sendPacket(Packet packet)
    {
        getHandle().b.sendPacket(packet);
    }

    public void sendPacket(AbstractPacket packet)
    {
        packet.sendPacket(getPlayer());
    }

    public void setTabHeaderFooter(String header, String footer)
    {
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter(
                new ChatComponentText(header),
                new ChatComponentText(footer)
        );

        sendPacket(packet);
    }

    /* guns */
    public Gun getGunInHand(IGame game)
    {
        StrikeStack stack = game.getGameSession(this).getInventory().getStack(getHeldSlot());

        if (stack.isEmpty()) {
            return null;
        }

        StrikeItem item = stack.top();
        if (item instanceof Gun)
            return (Gun) item;

        return null;
    }

    public StrikeItem getHeldItem(Game game) {
        return getInventory(game).getItem(getHeldSlot());
    }

    public Gun getPrimaryWeapon(Game game)
    {
        return getActiveSession(game).getInventory().getGun(0);
    }

    public Gun getSecondaryWeapon(Game game)
    {
        return getActiveSession(game).getInventory().getGun(1);
    }

    public int getVersion() {
        ViaAPI api = Via.getAPI();
        int version = api.getPlayerVersion(getPlayer());
        return version;
    }

    public void loadResources()
    {
        int clientVer = getVersion();
        MsdmPlugin.highlight("Load resources for " + getName() + " " + clientVer);
        if (clientVer >= ClientVersion.V1_9.getProtocolVersion()) {
            getPlayer().setResourcePack("http://static.betterms.odays.ky/bms_v.0.1.10.zip");
            return;
        }
        getPlayer().setResourcePack("http://static.betterms.odays.ky/bms_v.0.1.10_legacy.zip");
    }

    public void unloadResources() {
        int clientVer = getVersion();
        if (clientVer >= ClientVersion.V1_9.getProtocolVersion()) {
            getPlayer().setResourcePack("http://static.betterms.odays.ky/bms_empty_v.0.1.1.zip");
            return;
        }
        getPlayer().setResourcePack("http://static.betterms.odays.ky/bms_empty_v.0.1.1_legacy.zip");
    }

    public void throwGrenade(Game game, boolean wasLeftClick)
    {
        StrikeStack stack = getHeldSlot(game);
        if (!stack.isOf(StrikeItemType.GRENADE)) {
            return;
        }

        game.throwGrenade(this, wasLeftClick);
    }

    /* this method clears all game specific data from player */
    public void onGameQuit(Game game)
    {
        onTeamLeave(game);
    }

    public String getActualName()
    {
        return _actualName;
    }

    public boolean isOnline()
    {
        return getPlayer() != null;
    }

    public void setBossbar(String string, float percent) {
        // bossbar broken
        //BossBarAPI.setMessage(getPlayer(), string, percent);
    }

    public void sendHotBar(String message)
    {
        if (!isOnline())
        {
            System.out.println("[WARN] Tried to send hotbar to offline player");
            return;
        }
        HotbarMessager.sendActionbar(getPlayer(), message);
    }

    public void sendMessage(TextComponent text)
    {
        if (getPlayer() == null) {
            MsdmPlugin.warn("Failed to deliver message to offline player: " + text);
            return;
        }

        getPlayer().spigot().sendMessage(text);
    }

    public void sendMessage(String message) {
        if (getPlayer() == null) {
            MsdmPlugin.warn("Failed to deliver message to offline player: " + message);
            return;
        }

        getPlayer().sendMessage(message);
    }

    public void sendMessage(Message... builder) {
        getPlayer().spigot().sendMessage(Arrays.stream(builder).map(Message::build).toArray(TextComponent[]::new));
    }

    public void sendMessage(MsgSender sender, TextComponent message)
    {
        sendMessage(sender.form(message));
    }

    public void sendMessage(MsgSender sender, Message message)
    {
        sendMessage(sender.form(message.build()));
    }

    public void sendMessage(MsgSender sender, String message)
    {
        sendMessage(sender.form(message));
    }

    @LocalEvent
    private void onDataLoaded(PlayerDataReceivedEvent e) {
        if (!e.getPlayer().isOnline()) {
            return;
        }

        this.updateTabName(getGame());
    }

    ArrayList<Task> stunTasks = new ArrayList<>();


    @LocalEvent
    private void playerJoin(PlayerJoinServerEvent e) {
        // if player logged out during stun
        // movement speed may get messed up
        cancelStun();
    }

    public void cancelStun() {
        float normalWalkSpeed = 0.2f;

        if (getPlayer() == null) {
            return;
        }

        MsdmPlugin.info("Cancel stun for " + getPlayer().getName());
        getPlayer().removePotionEffect(PotionEffectType.JUMP);
        getPlayer().setWalkSpeed(normalWalkSpeed);

        for (Task task : stunTasks) {
            if (task.isRunning()) {
                task.cancel();
            }
        }
        stunTasks.clear();
    }

    public void stun(int stunTicks) {

        if (getPlayer() == null) {
            return;
        }

        // if player was already stunned,
        // cancel previous stun
        if (!stunTasks.isEmpty()) {
            cancelStun();
        }

        // reduce jump height
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, 253));

        // cancel upwards momentum
        double y = getPlayer().getVelocity().getY();
        getPlayer().setVelocity(new Vector(0, y > 0 ? 0 : y, 0));

        float walkSpeed = 0.05f;
        float normalWalkSpeed = 0.2f;
        float walkSpeedStep = (normalWalkSpeed - walkSpeed) / stunTicks;

        for (int tick = 0; tick < stunTicks; tick++) {

            final int finalTick = tick;

            var task = new Task(() -> {
                if (getPlayer() == null) {
                    return;
                }

                if (finalTick == stunTicks - 1) {
                    cancelStun();
                } else {
                    getPlayer().setWalkSpeed(walkSpeed + walkSpeedStep * finalTick);
                }
            }, tick).run();

            stunTasks.add(task);
        }
    }

    public boolean isSneaking() {
        return getPlayer().isSneaking();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StrikePlayer strikePlayer) {
            return strikePlayer.getUuid() == this.getUuid();
        }
        return false;
    }

    public PlayerStatus getStatus(Game game) {
        return game.getGameSession(this).getStatus();
    }

    public boolean isBot() {
        return false;
    }

    public void giveShield() {
        getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
    }
    public void removeShield() {
        getPlayer().getInventory().setItemInOffHand(null);
    }

    public double getHealth(Game game) {
        var session = game.getGameSession(this);
        if (session == null) {
            return 0;
        }

        if (!session.isAlive()) {
            return 0;
        }

        return getPlayer().getHealth();
    }

    public String getShortChatName(Game game) {
        StrikeTeam team = game.getGameSession(this).getRoster().getTeam();
        if (team == null) {
            return getName();
        }
        return team.getColor() + getName() + ChatColor.RESET;
    }

    public void setCompassTarget(Location loc) {
        getPlayer().setCompassTarget(loc);
    }

    public void setMovementSpeed(float i) {
        getPlayer().setWalkSpeed(i);
    }
    public void disableJump(int ticks) {
        getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, ticks, 250));
    }

    public boolean hasLineOfSight(StrikePlayer player) {
        return getPlayer().hasLineOfSight(player.getPlayer());
    }

    public void pdcSetFeature(ACFeature feature, boolean active) {
        PodcrashIntegration.setFeature(this, feature, active);
    }

    public void pdcSetTabListMap(GameMap map) {
        String name =  map.getName();

        // capitalize first letter
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        PodcrashIntegration.setTabListMap(this, C.cGold + "MineStrike - " + name);
    }

    public void pdcCreateTabVariable(String name) {
        PodcrashIntegration.createTabVariable(this, name);
    }

    public void pdcCreateTeam(InGameTeamData team) {
        PodcrashIntegration.createTeam(this, team.getNiceName(), (byte) team.getTeam().getChatColor().getChar());
    }

    public void pdcAssignTeam(StrikePlayer player, InGameTeamData team) {
        PodcrashIntegration.assignTeam(this, player.getName(), team.getNiceName());
    }

    public void pdcSetTeamScore(InGameTeamData team, int score) {
        PodcrashIntegration.setTeamScore(this, team.getNiceName(), score);
    }

    public void pdcSetTabVariable(StrikePlayer player, String name, String value) {
        PodcrashIntegration.setTabVariable(this, player.getName(), name, value);
    }

    public void pdcResetAll() {
        PodcrashIntegration.resetAll(this);
    }

    public void addButton(HotbarButton button, int slot) {
        var player =  getPlayer();

        if (player == null)
            return;

        player.getInventory().setItem(slot, button.getItemStack(this, slot));
    }


    public void setBmsClientUsed(boolean clientUsed) {
        this.clientUsed = clientUsed;
    }

    public boolean isBmsClientUsed() {
        return clientUsed;
    }

    public void addTemporaryPermission(Permission permission, Object scope) {
        temporaryPermissions.putIfAbsent(scope, new HashSet<>());
        temporaryPermissions.get(scope).add(permission);
    }

    public HashMap<Object, HashSet<Permission>> getTemporaryPermissions() {
        return temporaryPermissions;
    }

    public void removeTemporaryPermission(Permission permission, Competitive game) {
        if (!temporaryPermissions.containsKey(game)) {
            return;
        }
        temporaryPermissions.get(game).remove(permission);
    }

    public void setLevel(int i) {
        if (getPlayer() == null) {
            return;
        }
        getPlayer().setLevel(i);
    }
}
