package obfuscate.network;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.ToBackendEvent;
import obfuscate.event.custom.internal.ConfirmEvent;
import obfuscate.event.custom.internal.PingOutEvent;
import obfuscate.event.custom.internal.BukkitInitEvent;
import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WebsocketClient {
    WebSocket websocket = null;
    private final String url;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Integer pingId = null;

    private boolean isUp = false;

    private final String sessionId;

    private final Queue<ToBackendEvent> queue = new PriorityQueue<>();

    private final List<Runnable> onReconnectHandlers = new ArrayList<>();

    public boolean isUp() {
        return isUp;
    }

    public WebsocketClient(String url) {
        this.url = url;
        this.sessionId = UUID.randomUUID().toString();

        // Initiate connection
        // (make sure sessionId is created before lol)
        this.reconnect();

        // ping backend every 30 seconds
        scheduler.scheduleAtFixedRate(this::ping, 30, 30, TimeUnit.SECONDS);
    }

    public void onReconnect(Runnable r) {
        onReconnectHandlers.add(r);
    }

    private void ping() {

        // connection is not established
        if (!isUp) {
            return;
        }

        if (this.pingId != null) {
            MsdmPlugin.logger().info("Didn't receive PING response to ID " + this.pingId + " in 30 seconds. Reconnecting to the backend");
            this.reconnect();
        }
        this.pingId = new Random().nextInt();
        this.sendMessage(new PingOutEvent(this.pingId));
    }

    public void reconnect() {
        MsdmPlugin.logger().info("Reconnect");

        if (websocket != null && websocket.isOpen()) {
            MsdmPlugin.logger().warning("There is an open connection already. Closing it...");
            websocket.disconnect();
            MsdmPlugin.logger().warning("Connection closed. Connecting in 5 seconds...");
            scheduler.schedule(this::connect, 5, TimeUnit.SECONDS);
        } else {
            MsdmPlugin.logger().info("Connecting...");
            this.connect();
        }
    }

    private void connect() {
        final String socketURL = url + "/ws/connect?session_id=" + sessionId;

        try {
            MsdmPlugin.logger().info("Try to connect to the websocket @ " + socketURL);
            websocket = new WebSocketFactory()
                    .setConnectionTimeout(10000) // 10 seconds timeout
                    .createSocket(socketURL)
                    .addListener(new WebSocketAdapter() {
                        @Override
                        public void onTextMessage(WebSocket ws, String message) {
                            onMessage(ws, message);
                        }
                    })
                    .connect();
            isUp = true;
            MsdmPlugin.logger().info("Send init");
            sendMessage(new BukkitInitEvent("Whatever"));

            // send all events. if connection goes down at any point
            // newly added messages won't be sent
            for (int i = 0; i < queue.size(); ++i) {
                MsdmPlugin.logger().info("Send queued");
                this.sendMessage(queue.poll());
            }

            for (Runnable r : onReconnectHandlers) {
                r.run();
            }

        } catch (WebSocketException | IOException e) {
            MsdmPlugin.logger().info("Error connecting to the websocket");
            isUp = false;
            e.printStackTrace();
            MsdmPlugin.logger().info("Could not connect to the backend websocket endpoint at " + url + ". Retrying in 30 seconds");
            scheduler.schedule(this::reconnect, 30, TimeUnit.SECONDS);
        }
    }

    public void onMessage(WebSocket ws, String message) {
//        MsdmPlugin.highlight("Received WS event: " + message);
        HashMap<String, Object> eventDataJson;
        try {
            eventDataJson = (HashMap<String, Object>) new JSONParser().parse(message);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            BackendEventManager.parseAndTrigger(eventDataJson);
        } catch (Exception e) {
            MsdmPlugin.logger().log(Level.SEVERE, "[WS] Could not create and trigger event:");
            e.printStackTrace();
        }
    }

    public void sendConfirmBackendMessage(Integer messageId, HashMap<String, Object> payload) {
        sendMessage(new ConfirmEvent(messageId, payload));
    }

    /** Send message to the backend via WebSocket.
     * Generally events should be sent to the backend only through HTTP endpoint.
     * Response event is not implemented for Plugin -> Backend WS communication
     * */
    private void sendMessage(ToBackendEvent e) {

        Gson gson = new Gson();

        // serialize event to be sent to backend
        var map = new HashMap<String, Object>();
        map.put("type", e.getName());
        map.put("data", e.getPayload());

        String message = gson.toJson(map);

        if (!isUp) {
            MsdmPlugin.logger().warning("Could not send WS message: " + message + ". Connection is not established");
            queue.add(e);
            return;
        }

        if (websocket == null || !websocket.isOpen()) {
            MsdmPlugin.warn("Websocket connection is closed. Reconnecting");
            reconnect();
        }

        try {
            websocket.sendText(message);
            //MsdmPlugin.logger().info("Sent WS message: " + message);
        } catch (Exception ex) {
            ex.printStackTrace();
            queue.add(e);
            MsdmPlugin.logger().info("Failed to send message to the backend. Reconnecting");
            this.reconnect();
        }

    }

    /** Handle ping message */
    public void receivePing(Integer pingId) {
        if (!pingId.equals(this.pingId)) {
            MsdmPlugin.logger().warning("Received PING response to ID " + pingId + " but expected " + this.pingId);
        }
        this.pingId = null;
    }

    public String getSessionId() {
        return sessionId;
    }
}