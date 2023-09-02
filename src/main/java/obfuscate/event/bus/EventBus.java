package obfuscate.event.bus;

import obfuscate.util.java.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public class EventBus {

    private static Integer subCount = 0;

    private static final HashMap<Class<?>, HashMap<Integer, Pair<Integer, Function<Object, Boolean>>>> listeners = new HashMap<>();

    public static <T> Integer addEventHandler(Class<T> eventClass, Function<T, Boolean> callback, Integer callLimit) {
        listeners.computeIfAbsent(eventClass, k -> new HashMap<>()).put(++subCount, new Pair<>(callLimit, x -> callback.apply((T) x)));
        return subCount;
    }

    public static <T> Integer addEventHandler(Class<T> eventClass, Function<T, Boolean> callback) {
        return addEventHandler(eventClass, callback, null);
    }

    public static <T> Integer waitForEvent(Class<T> eventClass, Function<T, Boolean> callback) {
        return addEventHandler(eventClass, callback, 1);
    }

    public static void pushEvent(Object event) {

        HashMap<Integer, Pair<Integer, Function<Object, Boolean>>> handlers = listeners.get(event.getClass());

        if (handlers == null) {
            return;
        }

        ArrayList<Integer> toRemove = new ArrayList<>();

        for (Integer subId : handlers.keySet()) {
            Pair<Integer, Function<Object, Boolean>> handler = handlers.get(subId);

            if (handler.getFirst() != null) {
                if ( handler.getFirst() <= 0) {
                    toRemove.add(subId);
                    continue;
                }
                handler.setFirst(handler.getFirst() - 1);
            }

            var func = handler.getSecond();
            var ret = func.apply(event);

            if (ret != null && ret) {
                toRemove.add(subId);
            }
            if (handler.getFirst() != null && handler.getFirst() <= 0) {
                toRemove.add(subId);
            }
        }

        for (Integer subId : toRemove) {
            handlers.remove(subId);
        }

    }

    public static void unsubscribe(Integer subId) {
        for (Class<?> eventClass : listeners.keySet()) {
            listeners.get(eventClass).remove(subId);
        }
    }
}
