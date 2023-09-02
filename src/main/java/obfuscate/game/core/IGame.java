package obfuscate.game.core;

import obfuscate.event.CustomListener;
import obfuscate.event.custom.round.RoundWinEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.config.GameConfiguration;
import obfuscate.game.core.traits.SharedGameContext;
import obfuscate.game.damage.DamageLog;
import obfuscate.game.damage.DamageManager;
import obfuscate.game.player.BotPlayer;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.GameStateInstance;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.gamemode.registry.GameMode;
import obfuscate.mechanic.item.guns.Bullet;
import obfuscate.mechanic.version.hitbox.Hitbox;
import obfuscate.message.MsgSender;
import obfuscate.message.MsgType;
import obfuscate.network.models.responses.IntentResponse;
import obfuscate.team.PlayerGroup;
import obfuscate.game.restore.RestoreManger;
import obfuscate.game.shop.ShopManager;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.utility.grenade.Grenade;
import obfuscate.team.InGameTeamData;
import obfuscate.team.StrikeTeam;
import obfuscate.util.Promise;
import obfuscate.util.time.Task;
import obfuscate.world.GameMap;
import obfuscate.world.TempMap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public interface IGame extends CustomListener {
    void registerRoundTask(Task task);

    void stopTasks();

    /**
     * called to prepare game for new round, at this point previous round definitely ended
     * */
    void resetGameInfo();

    /** force-starts game.
     * @param seconds delay in seconds*/
    void forceStart(int seconds);

    InGameTeamData getOther(InGameTeamData roster);

    StrikeTeam getPlayerTeam(StrikePlayer player);

    InGameTeamData getPlayerRoster(StrikePlayer player);

    InGameTeamData getRoster(StrikeTeam team);

    /** Duplicate part of inventory into the world. Player's inventory stays untouched */
    void dropInventory(StrikePlayer player);

    /** this method is executed to join player to game.
     * its private so can only be called by event listener.
     * Players should be added to game only via gaming lobby */
    void joinPlayer(StrikePlayer player, InGameTeamData team, boolean spectate);

    void updateGrenadesAndBullets();

    void startGame();

    boolean is(ConfigField field);

    Integer get(ConfigField field);

    /** Makes sure player still spectates it's registered spectator target. Used on player sneak event */
    void updateSpectator(StrikePlayer spectator);

    StrikePlayer getSpectatedPlayer(StrikePlayer spectator);

    void spectateNext(StrikePlayer spectator);

    void respawn(StrikePlayer player, boolean spec);

    /** Makes player spectate in Free-Cam mode or after teammate. */
    void spawnSpectator(StrikePlayer player);

    /** teleport player to position. This method won't change player status to spectate */
    void respawnAlive(StrikePlayer player);

    int getMaxRounds();

    int getRoundNumber();

    void setRound(int round);

    boolean canMove(StrikePlayer player);

    boolean canInteractWithItems(StrikePlayer player);

    int getMaxPlayers();

    int getMinPlayers();

    DamageManager getDamageManager();

    InGameTeamData getCurrentRoundWinner();

    void winRound(StrikeTeam team, RoundWinEvent.Reason reason);

    void broadcast(InGameTeamData roster, String message);
    void broadcast(MsgSender sender, String message, MsgType type, Object ... args);

    void registerThrownGrenade(Grenade grenade);

    public GameStateInstance getGameState();

    public void setLeftDuration(int duration);

    void destroy();

    void restart();

    boolean canJoin(StrikePlayer player, boolean spectator);

//    GameStateManager getStateManager();

    SharedGameContext getSharedContext();

    String getGameName();

    TempMap getTempMap();

    GameMap getGameMap();

    void setRoundEndState();

    List<StrikePlayer> getAlivePlayers();

    List<StrikePlayer> getAlivePlayers(PlayerGroup<StrikePlayer> group);

    /** @return all players that are in game world right now */
    List<StrikePlayer> getOnlinePlayers();

    /** @return players who are on game server participating and are dead */
    List<StrikePlayer> getDeadPlayers();

    /** @return players who are online on game server and are spectating */
    List<StrikePlayer> getOnlineSpectators();

    /** @return players who participated in game but left and not online in game server at the moment */
    List<StrikePlayer> getAwayPlayers();

    void setRandomSpectateTarget(StrikePlayer spectator);

    List<StrikePlayer> getCoaches();

    GameConfiguration getConfig();
    int getRoundTime();

    void deregisterDroppedItem(Entity ent);

    void deregisterDroppedItem(StrikeItem ent);

    /** check if player not in game */
    boolean away(StrikePlayer player);

    void registerIncendiary(Location loc, long l);

    void registerFireBlock(Block block, Grenade source, Long expires);

    int getTotalScore(StrikeTeam team);
    int getTotalScore(InGameTeamData team);
    int getRelativeScore(InGameTeamData team);
    int getRelativeScore(StrikeTeam team);
    Grenade getFireStarterAt(@Nonnull Block block);

    void registerSmoke(Location loc, long duration);

    void pickUpFromGround(StrikePlayer player, StrikeItem item, Item ent);

    void deleteDropped();

    void deleteNades();

    StrikeItem getItem(Item item);

    void registerDroppedItem(Item ent, StrikeItem strikeItem);

    void breakGlass(Block block);

    StrikePlayer getParticipant(String ign);

    Location getRespawnPosition(StrikePlayer player);

    Location getRandomRespawn(StrikePlayer player);

    boolean isEnded();

    List<StrikePlayer> getWhitelist();

    PlayerGroup<StrikePlayer> getBlacklist();


    GameSession getGameSession(StrikePlayer player);

    void setPlayerReady(StrikePlayer player);

    boolean readyStateRequiredByConfig();

    boolean isPlayerReady(StrikePlayer player);

    boolean everyoneReady();

    ArrayList<StrikePlayer> getNotReadyPlayers();

    boolean canStart();

    boolean hasEnoughPlayers();

    boolean isInProgress();

    DamageLog getDamageLog();

    RestoreManger getRestore();

    StrikePlayer getPlayer(String ign);

    InGameTeamData[] getRosters();

    ShopManager getShopManager();

    /** Send command to backend asking to leave */
    Promise<? extends IntentResponse> tryLeavePlayer(StrikePlayer player);

    /** Send command to backend asking to join */
    Promise<?> tryJoinPlayer(StrikePlayer player, boolean spec);

    Promise<?> tryJoinPlayer(StrikePlayer player, boolean spec, StrikeTeam team);

    Promise<?> tryJoinPlayer(StrikePlayer player, StrikeTeam team, boolean spec);

    boolean shouldSwapSides();

    boolean isInitialized();

    List<BotPlayer> getBots();

    int getStateConfigDuration();

    public void setGameState(GeneralGameStage stage);
    public void setGameState(GeneralGameStage stage, Integer seconds);

    public boolean startNextPause();

    public boolean hasNextPause();

    Scoreboard getPlayerScoreboard(StrikePlayer strikePlayer);

    boolean isPaused();
    GameMode getMode();

    Hitbox getPlayerHitbox(Bullet bullet);

    void setGameState(GameStateInstance deathMatch, Integer duration);
}
