package obfuscate.network;

import obfuscate.MsdmPlugin;
import obfuscate.event.CustomListener;
import obfuscate.event.LocalEvent;
import obfuscate.event.custom.ToBackendEvent;
import obfuscate.event.custom.backend.FromBackendEvent;
import obfuscate.event.custom.backend.internal.PingInEvent;
import obfuscate.game.core.Game;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.serialize.load.*;
import obfuscate.network.models.responses.EventResponse;
import obfuscate.network.models.requests.ExpectedElo;
import obfuscate.network.models.responses.ResponseManager;
import obfuscate.util.Promise;
import obfuscate.util.time.Task;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class BackendManager implements CustomListener {
    /** A utility class that helps to manage network connections */

    public final HashMap<ObjectId, SyncableObject> trackedModels = new HashMap<>();
    private WebsocketClient conn;

    public BackendManager() {}

    public WebsocketClient getConnection() {
        return conn;
    }

    public void connect() {
        Logger.info("Connecting to the backend...", Tag.NET_EVENTS);
        conn = new WebsocketClient(MsdmPlugin.Config.getSocketUrl());
        Logger.info("Connection opened", Tag.NET_EVENTS);

        // when connection restarts, reload Server model to refresh the subscription
        // as well as to get the latest data
        conn.onReconnect(() -> loadModel(MsdmPlugin.getGameServer()));
    }

    public SyncableObject getById(ObjectId pk) {
        return trackedModels.get(pk);
    }

    public List<SyncableObject> getByEntity(String ent) {
        return trackedModels.values().stream().filter(x -> x.getModelName().equals(ent)).toList();
    }

    private static String objectIdToPrimitive(ObjectId objectId) {
        return objectId.getEntity().toLowerCase().replace("_", "") + ":" + objectId.getObjId();

    }

    private final HashMap<String, ArrayList<SyncableObject>> dependantsByPrimitive = new HashMap<>();

    /** Gets syncable objects that are affected by this object id update */
    public ArrayList<SyncableObject> getDependants(ObjectId updatedId) {

        ArrayList<SyncableObject> affectedObjets = new ArrayList<>();

        for (String dependencyKey : dependantsByPrimitive.keySet()) {

            String[] dependencyKeyParts = dependencyKey.split(":");
            String dependencyEntity = dependencyKeyParts[0];
            String dependencyId = dependencyKeyParts[1];

            if (!dependencyEntity.equals(updatedId.getEntity().toLowerCase().replace("_", ""))) {
                continue;
            }
            if (!Objects.equals(dependencyId, "null") && !dependencyId.equals(updatedId.getObjId().toString())) {
               continue;
            }

            affectedObjets.addAll(dependantsByPrimitive.get(dependencyKey));

        }

        return affectedObjets;
    }

    /** Mark object as depending on this object id */
    public void registerDependency(ObjectId updatedId, SyncableObject dependant) {
        var dependants = dependantsByPrimitive.computeIfAbsent(objectIdToPrimitive(updatedId), k -> new ArrayList<>());
        dependants.add(dependant);
    }

    /** Make HTTP call to see if backend is online */
    public Promise<Boolean> isHTTPUp() {
        Promise<Boolean> result = new Promise<>();
        try {
            makeHttpRequest("status", "GET", new HashMap<>()).thenSync(
                    (x) -> {
                        result.fulfill(true);
                        return x;
                    }
            );
        } catch (Exception e) {
            result.fulfill(false);
        }

        return result;
    }

    /** Make GraphQL query to get model data.
     * Called with existing instance so can be used to refresh data as well.
     * Basically a wrapper on top of load(Object json) that gets the data from backend and manages all th e promises */
    public <T extends SyncableObject> Promise<T> loadModel(T instance) {
        Promise<T> fullLoadPromise = new Promise<>();

        // make graphql request
        HashMap<String, Object> args = new HashMap<>();
        String query = instance.getGraphQlQuery();

        args.put("query", query);

//        MsdmPlugin.info("Query: " + query);

        HashMap<String, String> headers = new HashMap<>();
        headers.put("session_id", conn.getSessionId());

        Promise<HashMap<String, Object>> content = makeHttpRequest("graphql/", "POST", args, headers);

        content.thenSync((data) -> {
//            String fromDataPretty = new GsonBuilder().setPrettyPrinting().create().toJson(data);
//            MsdmPlugin.logger().info("Backend.loadModel(" + instance.getModelName() + ":" + instance.getId() + ") FROM " + fromDataPretty);

            // make calls to Minecraft from scheduler because presumably we have a new thread somewhere here.
            new Task(() -> {

                if (!instance.getInitializationPromise().isFulfilled()) {
                    instance.getInitializationPromise().fulfill(null);
                }

                Object errors = data.get("errors");

                if (errors != null) {
                    Map<String, Object> extras = new HashMap<>();
                    extras.put("query", query);
                    extras.put("errors", errors);
                    Logger.severe("There was an error getting graphql data. Query: " + query + " Errors: " + errors, Tag.NET_EVENTS, extras);
                }

                Object modelData = ((HashMap<String, Object>)data.get("data")).get(instance.getModelName());

                // since we are doing recursive updates, we should wait for every field to propagate.
                // note: this will result in longer wait only for initial load, after first time
                // promise will be already resolved, therefore resulting in instant callback call
                instance.load(modelData).thenSync( y -> {
                        // fulfill before triggering event,
                        // because important stuff defined in that .then()
                        // such as saving PK to model registry. up to this moment
                        // there only was uuid
                        fullLoadPromise.fulfill(instance);

//                        MsdmPlugin.highlight("Backend.loadModel(" + instance + ") : Full load done");
                        // make instance trigger own event
                        instance.onModelUpdate();

                        return null;
                    }
                );
            }, 0).run();
            return null;
        });

        return fullLoadPromise;
    }

    public Promise<Integer> getPlayerId(UUID uuid) {
        Promise<Long> promise = makeHttpRequest("api/player/id/" + uuid, "GET", new HashMap<>());
        return promise.thenSync(Long::intValue);
    }

    public Promise<Integer> getBotPlayerId(UUID uuid) {
        Promise<Long> promise = makeHttpRequest("api/player/id", "GET", new HashMap<>());
        return promise.thenSync(Long::intValue);
    }

    /**
     * Send out an event that happened on bukkit side to the backend
     * */
    public @Nullable Promise<? extends EventResponse> sendEvent(ToBackendEvent e) {
        /* Send arbitrary event to the backend */
        Logger.info("Sending event to the Backend: " + e.getName(), e.getPayload(), Tag.NET_EVENTS);
        HashMap<String, Object> toSend = new HashMap<>();
        toSend.put("type", e.getName());
        toSend.put("data", e.getPayload());
        return makeHttpRequest("api/bukkit/event", "POST", toSend).thenAsync(
            x -> {
                Logger.info("Received response for event " + e.getClass().getSimpleName(), x, Tag.NET_EVENTS);
                return ResponseManager.createResponse(
                        e,
                        (HashMap<String, Object>) ((HashMap<String, Object>) x).get("response")
                );
            }
        );
    }

    public void confirmBackendEvent(FromBackendEvent e, HashMap<String, Object> payload) {
        /* Send a confirmation event to the backend that we received the event */
        conn.sendConfirmBackendMessage(e.getMsgId(), payload);
    }

    private <T> Promise<T> makeHttpRequest(
            String url,
            String method,
            HashMap<String, Object> data
    ) {
        return makeHttpRequest(url, method, data, new HashMap<>());
    }

    private <T> Promise<T> makeHttpRequest(
            String url,
            String method,
            HashMap<String, Object> data,
            HashMap<String, String> headers
    ) {
        Promise<T> promise = new Promise<>();

        String backendURL = MsdmPlugin.Config.getBackendUrl();

        if (backendURL == null) {
            throw new RuntimeException("Backend URL is not set in server.yml");
        }

        // MsdmPlugin.getInstance().getLogger().info("Making request to " + backendURL + "/" + url);

        JSONObject jsonData = new JSONObject(data);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(backendURL + "/" + url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");

        if (headers.size() > 0) {
            requestBuilder = requestBuilder.headers(
                headers.entrySet().stream().map(
                        x -> new String[]{x.getKey(), x.getValue()}
                ).flatMap(Arrays::stream).toArray(String[]::new)
            );
        }

        if (method.equals("POST")) {
            requestBuilder = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonData.toJSONString()));
        } else {
            requestBuilder = requestBuilder.GET();
        }

        HttpRequest request = requestBuilder.build();

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1).build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())

            .thenAccept((HttpResponse x)-> {
                if (x.statusCode() != 200) {
                    Logger.severe("[HTTP] Bad response: " + x.statusCode() + " " + x.body());
                    return;
                }
                try {
                    Object json = new JSONParser().parse((String) x.body());
//                    MsdmPlugin.highlight("[HTTP] Response: " + json);
                    promise.fulfill((T) json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        return promise;
    }

    /** This should be used carefully, it will create an instance, but it will not start sync process */
    public SyncableObject getOrCreateModelWithoutSync(Class<? extends SyncableObject> targetModel, ObjectId pk) {
        SyncableObject model = getById(pk);

        if (model == null) {
            try {
                model = targetModel.getDeclaredConstructor().newInstance();
                addTrackedModel(model, pk);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
                Logger.severe("Reflection Error while creating model instance of: " + targetModel.getName() + " with pk " + pk, e);
                throw new RuntimeException("[BetterMS] Reflection Error while creating model instance of " + targetModel.getName() + " with pk " + pk);
            }
        }
        return model;
    }

    public <T extends SyncableObject> ObjectLoadFuture<T> getOrCreateModel(Class<T> targetModel, ObjectId pk) {

        // if model exists in cache, return it
        if (getById(pk) != null) {
            SyncableObject existing = getById(pk);
            var promise = existing.waitFullyInitialized();
            return new ObjectLoadFuture<>((T) existing, promise);
        }

        SyncableObject model = getOrCreateModelWithoutSync(targetModel, pk);

        // run initial sync for this model
        Promise<?> modelLoadedPromise = model.setId(pk);

        return new ObjectLoadFuture<>((T) model, modelLoadedPromise);
    }

    public void addTrackedModel(SyncableObject syncableModel, ObjectId key) {
        trackedModels.put(key, syncableModel);
    }

    public Struct createLoadableModel(Class<? extends Struct> model) {
        try {
            return model.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Promise<ExpectedElo> getExpectedElo(Game game) {
        HashMap<String, Object> args = new HashMap<>();
        return makeHttpRequest("api/elo/predict/" + game.getId().getObjId(), "GET", args).thenAsync(
            x -> {
                var y = new ExpectedElo();
                return y.load(x).thenSync(z -> y);
            }
        );
    }

    public void deleteModel(SyncableObject model) {
        trackedModels.remove(model.getId());
    }

    public List<Struct> getCachedModels() {
        return new ArrayList<>(trackedModels.values());
    }

    @LocalEvent
    private void onPing(PingInEvent e) {
        getConnection().receivePing(e.pingId);
    }

    public Promise<?> refreshAllModels() {

        Promise<?> chain = Promise.Instant();
        for (SyncableObject model : trackedModels.values()) {
            chain = chain.thenAsync(x -> loadModel(model));
        }
        return chain;

    }


    public List<String> getEntityNames() {
        var all = trackedModels.values().stream().map(x -> x.getId().getEntity()).toList();
        return new ArrayList<>(new HashSet<>(all));
    }

    public List<Integer> getSyncedIds(String ent) {
        return trackedModels.values().stream().filter(x -> x.getId().getEntity().equals(ent)).map(x -> x.getId().getObjId()).toList();
    }
}
