package obfuscate.game;

import obfuscate.MsdmPlugin;
import obfuscate.event.CustomListener;
import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.game.GameDestroyEvent;
import obfuscate.event.custom.intent.GameDeleteIntentEvent;
import obfuscate.event.custom.lobby.PlayerJoinHubEvent;
import obfuscate.event.custom.network.ModelEvent;
import obfuscate.event.custom.player.PlayerJoinGameEvent;
import obfuscate.event.custom.player.PlayerLeaveGameEvent;
import obfuscate.event.custom.player.PlayerLeaveServerEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.registry.OnlinePlayerDataRegistry;
import obfuscate.gamemode.Competitive;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Model(name = "server")
public class Server extends SyncableObject implements CustomListener
{

    @Loadable(field = "games")
    private List<Competitive> games;

    private List<Hub> hubs = new ArrayList<>();

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
        for (StrikePlayer player : playerGames.keySet()){
            MsdmPlugin.logger().info("- " + player.getName() + " in " + playerGames.get(player));
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

    private final HashMap<StrikePlayer, Game> playerGames = new HashMap<>();
    private final OnlinePlayerDataRegistry onlinePlayerDataRegistry = new OnlinePlayerDataRegistry();

    @LocalEvent
    private Promise<?> handleSyncEvent(ModelUpdateEvent e) {
//        MsdmPlugin.highlight("ModelUpdateEvent: " + e.getUpdatedObjectId());

        if (e.getType().equals("Delete")) {
            MsdmPlugin.info("ModelUpdateEvent: Received delete event for " + e.getUpdatedObjectId());
            return Promise.Instant();
        }

        BackendManager Backend = MsdmPlugin.getBackend();
        SyncableObject obj = Backend.getById(e.getUpdatedObjectId());

        var affectedObjects = Backend.getDependants(e.getObjectId());

        ArrayList<Promise<?>> promises = new ArrayList<>();
        for (SyncableObject object : affectedObjects) {
            promises.add(
                Backend.loadModel(object)
            );
        }

        // we received update event for model we do not track
        if (obj == null) {
            MsdmPlugin.warn("Received update for model we do not track: " + e.getUpdatedObjectId());
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
        return playerGames.get(player);
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

    @LocalEvent(cascade = true, priority = LocalPriority.POST) // cascade so we handle both join and reconnect
    private void onPlayerJoin(PlayerJoinGameEvent e)
    {
        playerGames.put(e.getPlayer(), e.getGame());
        UtilPlayer.hideAllExceptSameLobby(e.getPlayer());
    }

    @LocalEvent(priority = LocalPriority.POST)
    private void onPlayerJoinHub(PlayerJoinHubEvent e) {
        UtilPlayer.hideAllExceptSameLobby(e.getPlayer());
    }

    @LocalEvent
    private void onPlayerLeave(PlayerLeaveGameEvent e)
    {
        playerGames.remove(e.getPlayer());
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

    public Promise<? extends IntentResponse> deleteGame(Competitive game) {
        return new GameDeleteIntentEvent(game).trigger();
    }
}
