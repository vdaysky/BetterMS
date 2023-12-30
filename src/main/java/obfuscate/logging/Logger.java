package obfuscate.logging;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.MonitoredResource;
import com.google.cloud.TransportOptions;
import com.google.cloud.logging.*;
import obfuscate.MsdmPlugin;
import obfuscate.util.serialize.ObjectId;
import obfuscate.util.serialize.load.SyncableObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class Logger {

    static Logging logging = LoggingOptions.newBuilder().setCredentials(MsdmPlugin.Config.getCredentials()).build().getService();

    private static HashMap<String, Object> contextMapper(Object item, HashMap<String, Object> context) {

        if (item instanceof SyncableObject model) {
            String name = model.getModelName();
            ObjectId objId = model.getId();
            if (objId == null) {
                context.put(name.toLowerCase() + "_id", null);
            } else {
                context.put(name.toLowerCase() + "_id", objId.getObjId());
            }
        }

        if (item instanceof Exception ex) {
            context.put("exception", ex.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            context.put("stacktrace", sw.toString());
        }

        if (item instanceof Map map) {
            context.putAll(map);
        }

        if (item instanceof Tag tag) {
            context.put("tag", tag.name());
        }

        return context;
    }

    public static void standardLog(String message, Severity level, Set<Object> context, Map<String, Object> extra) {

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> contextMap = new HashMap<>();

        for (Object item : context) {
            contextMap = contextMapper(item, contextMap);
        }

        if (extra != null) {
            // extend map with extras
            contextMap.putAll(extra);
        }

        data.put("message", message);
        data.put("context", contextMap);

        var entry = LogEntry.newBuilder(Payload.JsonPayload.of(data))
                .setSeverity(level)
                .setLogName("MineStrike")
                .setResource(MonitoredResource.newBuilder("global").build())
                .build();

        logging.write(Collections.singleton(entry));
        logging.flush();
    }

    public static void info(String message, Set<Object> context, Map<String, Object> extra) {
        standardLog(message, Severity.INFO, context, extra);
    }

    public static void info(String message, Object... context) {
        standardLog(message, Severity.INFO, new HashSet<>(List.of(context)), null);
    }

    public static void error(String message, Set<Object> context, Map<String, Object> extra) {
        standardLog(message, Severity.ERROR, context, extra);
    }

    public static void error(String message, Object... context) {
        standardLog(message, Severity.ERROR, new HashSet<>(List.of(context)), null);
    }

    public static void warning(String message, Set<Object> context, Map<String, Object> extra) {
        standardLog(message, Severity.WARNING, context, extra);
    }

    public static void warning(String message, Object... context) {
        standardLog(message, Severity.WARNING, new HashSet<>(List.of(context)), null);
    }

    public static void debug(String message, Set<Object> context, Map<String, Object> extra) {
        standardLog(message, Severity.DEBUG, context, extra);
    }

    public static void debug(String message, Object... context) {
        standardLog(message, Severity.DEBUG, new HashSet<>(List.of(context)), null);
    }

    public static void critical(String message, Set<Object> context, Map<String, Object> extra) {
        standardLog(message, Severity.CRITICAL, context, extra);
    }

    public static void critical(String message, Object... context) {
        standardLog(message, Severity.CRITICAL, new HashSet<>(List.of(context)), null);
    }

    public static void severe(String message, Set<Object> context, Map<String, Object> extra) {
        standardLog(message, Severity.CRITICAL, context, extra);
    }

    public static void severe(String message, Object... context) {
        standardLog(message, Severity.CRITICAL, new HashSet<>(List.of(context)), null);
    }
}

