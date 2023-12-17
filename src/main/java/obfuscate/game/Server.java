package obfuscate.game;

import obfuscate.Lobby;
import obfuscate.MsdmPlugin;
import obfuscate.event.CustomListener;
import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.game.GameDestroyEvent;
import obfuscate.event.custom.intent.CreateGameIntentEvent;
import obfuscate.event.custom.intent.GameDeleteIntentEvent;
import obfuscate.event.custom.lobby.PlayerJoinHubEvent;
import obfuscate.event.custom.network.ModelEvent;
import obfuscate.event.custom.player.PlayerJoinGameEvent;
import obfuscate.event.custom.player.PlayerLeaveGameEvent;
import obfuscate.event.custom.player.PlayerLeaveServerEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.game.core.Game;
import obfuscate.game.core.IGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.registry.OnlinePlayerDataRegistry;
import obfuscate.gamemode.Competitive;
import obfuscate.gamemode.registry.GameMode;
import obfuscate.hub.Hub;
import obfuscate.network.BackendManager;
import obfuscate.network.models.responses.IntentResponse;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;
import obfuscate.network.event.ModelUpdateEvent;
import obfuscate.util.Promise;
import obfuscate.util.UtilPlayer;
import obfuscate.util.time.Task;
import obfuscate.world.GameMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Model(name = "server")
public class Server extends SyncableObject implements CustomListener
{

    @Loadable(field = "games")
    private List<Competitive> games;

    private List<Hub> hubs = new ArrayList<>();
    private HashMap<StrikePlayer, IGame> ownedGames = new HashMap<>();

    private HashMap<IGame, Task> gameDestructionDelayedTasks = new HashMap<>();

    public Server() {
    }

    @Override
    public ArrayList<ObjectId> declaredDependencies() {
        var dependency = new ObjectId(
                null,
                "game",
                new ArrayList<>()
        );

        var dependencies = new ArrayList<ObjectId>();
        dependencies.add(dependency);
        return dependencies;
    }

    public void printState() {
        MsdmPlugin.logger().info("============= Games =================");
        for (StrikePlayer player : playerLobbies.keySet()){
            MsdmPlugin.logger().info("- " + player.getName() + " in " + playerLobbies.get(player));
        }

        for (Game game: games){
            MsdmPlugin.logger().info("Game: " + game.getId());
            MsdmPlugin.logger().info("    ============= Teams =================");
            MsdmPlugin.logger().info("    Team A:");
            for (StrikePlayer player : game.getTeamA().getPlayers()) {
                MsdmPlugin.logger().info("      - " + player.getName());
            }
            MsdmPlugin.logger().info("    Team B:");
            for (StrikePlayer player : game.getTeamB().getPlayers()) {
                MsdmPlugin.logger().info("      - " + player.getName());
            }
            MsdmPlugin.logger().info("    ============= Participants Online =================");
            for (StrikePlayer player : game.getOnlineDeadOrAliveParticipants()) {
                MsdmPlugin.logger().info("      - " + player.getName());
            }
        }
    }

    public List<Competitive> getGames() {
        return games;
    }

    private final HashMap<StrikePlayer, Lobby> playerLobbies = new HashMap<>();
    private final OnlinePlayerDataRegistry onlinePlayerDataRegistry = new OnlinePlayerDataRegistry();

    @LocalEvent
    private Promise<?> handleSyncEvent(ModelUpdateEvent e) {
//        MsdmPlugin.highlight("ModelUpdateEvent: " + e.getUpdatedObjectId());

        if (e.getType().equals("Delete")) {
            MsdmPlugin.info("ModelUpdateEvent: Received delete event for " + e.getObjectId());
            return Promise.Instant();
        }

        BackendManager Backend = MsdmPlugin.getBackend();
        SyncableObject obj = Backend.getById(e.getObjectId());

        var affectedObjects = Backend.getDependants(e.getObjectId());

        ArrayList<Promise<?>> promises = new ArrayList<>();
        for (SyncableObject object : affectedObjects) {
            promises.add(
                Backend.loadModel(object)
            );
        }

        // we received update event for model we do not track
        if (obj == null) {
            MsdmPlugin.warn("Received update for model we do not track: " + e.getObjectId());
            return null;
        }

        return Promise.gather(promises).thenSync(x -> x);
    }

    @LocalEvent(cascade = true)
    private void onPlayerJoinGameLeaveHub(PlayerJoinGameEvent e) {
        getFallbackServer().leave(e.getPlayer());
    }

    @LocalEvent
    private void onPlayerJoinGameLeaveHub(PlayerLeaveServerEvent e) {
        getFallbackServer().leave(e.getPlayer());
        e.getPlayer().setBmsClientUsed(false);
    }

    @LocalEvent
    private void deleteGame(GameDestroyEvent e) {
        // do some memory cleanup, it ain't much but at least it's honest work

        this.games.remove((Competitive) e.getGame());

        MsdmPlugin.getBackend().deleteModel(e.getGame());

        for (var session : e.getGame().getGameSessions()) {
            MsdmPlugin.getBackend().deleteModel(session);
        }

        MsdmPlugin.getBackend().deleteModel(e.getGame().getTeamA());
        MsdmPlugin.getBackend().deleteModel(e.getGame().getTeamB());
    }

    public Game getGame(StrikePlayer player)
    {
        if (playerLobbies.get(player) instanceof Game game) {
            return game;
        }
        return null;
    }

    public Lobby getLobby(StrikePlayer player) {
        return playerLobbies.get(player);
    }

    public Competitive getGame(int id) {
        for (Competitive game : games) {
            if (game.getId().getObjId() == id) return game;
        }
        return null;
    }

    public OnlinePlayerDataRegistry getOnlinePlayerDataRegistry() {
        return onlinePlayerDataRegistry;
    }

    public void triggerTimeEvent(TimeEvent.UpdateReason reason)
    {
        // create and iterate copy because this event may cause lobby deletion on update and comodification
        if (games != null) {

            for (Game game : games) {

                // we don't want to update this game before it is fully loaded
                // some stuff like world loading happens only after full model initialization
                if (!game.isInitialized()) {
                    continue;
                }

                new TimeEvent(game, reason).trigger();
            }
        }
    }

    private void setPlayerLobby(StrikePlayer player, Lobby lobby) {
        playerLobbies.put(player, lobby);
        player.clearBukkitInventory();
        player.setLevel(0);
        lobby.setupInventory(player);
    }

    @LocalEvent(cascade = true, priority = LocalPriority.POST) // cascade so we handle both join and reconnect
    private void onPlayerJoin(PlayerJoinGameEvent e)
    {
        setPlayerLobby(e.getPlayer(), e.getGame());
        UtilPlayer.hideAllExceptSameLobby(e.getPlayer());

        if (gameDestructionDelayedTasks.containsKey(e.getGame())) {
            gameDestructionDelayedTasks.get(e.getGame()).cancel();
        }
    }

    @LocalEvent(cascade = true, priority = LocalPriority.POST)
    private void onPlayerLeaveGame(PlayerLeaveGameEvent e) {
        if (e.getGame().getOnlinePlayers().isEmpty()) {
            rescheduleGameDestruction(e.getGame());
        }
    }

    @LocalEvent
    private void onGameDestroyed(GameDestroyEvent e) {
        // some cleanup
        gameDestructionDelayedTasks.remove(e.getGame());
    }

    private void rescheduleGameDestruction(IGame game) {
        if (gameDestructionDelayedTasks.containsKey(game)) {
            gameDestructionDelayedTasks.put(
                    game,
                    new Task(() -> deleteGame(game), 20 * 60 * 5).run()
            );
        }
    }

    public Promise<@Nullable Game> createGame(GameMap map, GameMode mode, boolean selfDestructs) {
        return new CreateGameIntentEvent(
                map.getName(),
                mode.name(),
                null
        ).trigger().thenSync(
            response -> {
                if (response.isSuccess()) {
                    Long gameId = (Long) response.getPayload().get("game_id");
                    var game = getGame(gameId.intValue());

                    if (selfDestructs) {
                        gameDestructionDelayedTasks.put(game, null);
                        rescheduleGameDestruction(game);
                    }

                    return game;
                }
                return null;
            }
        );
    }


    @LocalEvent(priority = LocalPriority.POST)
    private void onPlayerJoinHub(PlayerJoinHubEvent e) {
        setPlayerLobby(e.getPlayer(), e.getHub());
        UtilPlayer.hideAllExceptSameLobby(e.getPlayer());
    }

    public Hub getFallbackServer() {
        if (hubs.isEmpty()) {
            hubs.add(new Hub());
        }

        return hubs.get(0);
    }

    @Override
    public Class<? extends ModelEvent<? extends SyncableObject>> getFulfilledEvent() {
        return null;
    }

    public void addHub() {
        hubs.add(new Hub());
    }

    public Promise<? extends IntentResponse> deleteGame(IGame game) {
        return new GameDeleteIntentEvent(game).trigger();
    }

    public void registerOwnedGame(StrikePlayer player, Game game) {
        if (ownedGames.containsKey(player)) {
            IGame oldOwnedGame = ownedGames.get(player);
            deleteGame(oldOwnedGame);
        }
        ownedGames.put(player, game);
    }

    public @Nullable IGame getOwnedGame(StrikePlayer owner) {
        return ownedGames.get(owner);
    }

    public void shutdown() {
        // leave all players from all games
        for (var game : games) {
            for (var player : game.getOnlinePlayers()) {
                game.tryLeavePlayer(player);
            }
        }
        // delete all mps
        for (var game : ownedGames.values()) {
            deleteGame(game);
        }
    }
}
