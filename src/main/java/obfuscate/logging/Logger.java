package obfuscate.logging;

import com.google.cloud.MonitoredResource;
import com.google.cloud.logging.*;
import obfuscate.util.serialize.load.SyncableObject;

import java.util.*;

public class Logger {

    static Logging logging = LoggingOptions.getDefaultInstance().getService();

    private static HashMap<String, Object> contextMapper(Object item, HashMap<String, Object> context) {

        if (item instanceof SyncableObject model) {
            String name = model.getModelName();
            Integer id = model.getId().getObjId();
            context.put(name + "_id", id);
        }

        if (item instanceof Tag tag) {
            context.put("domain", tag.name());
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

        var entry = LogEntry.newBuilder(Payload.JsonPayload.of(contextMap))
                .setSeverity(level)
                .setLogName("MineStrike")
                .setResource(MonitoredResource.newBuilder("global").build())
                .build();

        logging.write(Collections.singleton(entry));
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
}

