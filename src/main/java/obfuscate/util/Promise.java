package obfuscate.util;

import obfuscate.MsdmPlugin;
import obfuscate.logging.Logger;
import obfuscate.logging.Tag;
import obfuscate.util.time.Scheduler;
import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

public class Promise<T> {

    private List<Function<T, Void>> callbacks = new ArrayList<>();
    private T value = null;

    private boolean fulfilled = false;

    private boolean comodLock = false;

    public Promise() {

    }

    public static <X> Promise<X> Instant(X result) {
        Promise<X> p = new Promise<>();
        // run in scheduler to synchronize with main thread
        Scheduler.runNextTick(() -> p.fulfill(result));
        return p;
    }

    /** Return instantly resolved promise */
    public static <X> Promise<X> Instant() {
        return Instant(null);
    }

    public void fulfill(T value) {

        if (fulfilled) {
            Logger.critical("Tried to fulfill a finished promise. Value: " + value, Tag.POTENTIAL_BUG);
            for (var x : Thread.currentThread().getStackTrace()) {
                Logger.info(x.toString());
            }
            return;
        }
        comodLock = true;
//        // print stack trace
//        StackTraceElement[] st = Thread.currentThread().getStackTrace();
//        for (StackTraceElement stackTraceElement : st) {
//            MsdmPlugin.highlight(stackTraceElement.toString());
//        }

        for (var callback : callbacks) {
            callback.apply(value);
        }
        comodLock = false;

        this.value = value;
        this.fulfilled = true;
    }

    public boolean isFulfilled() {
        return this.fulfilled;
    }

    /** Called to wrap promise with another promise. Promise will be executed after this one resolves. */
    public <R> Promise<R> thenAsync(Function<T, Promise<R>> doAfterThis) {
        if (comodLock) {
            Logger.critical("Tried to chain async promise while fulfilling promise: " + this, Tag.POTENTIAL_BUG);
            throw new RuntimeException("Tried to chain async promise while fulfilling promise: " + this);
        }
        Promise<R> promise = new Promise<>();

        // todo check if this actually works, looks too simple lol
        if (isFulfilled()) {
            return doAfterThis.apply(value);
        }

        this.callbacks.add(y -> {
            Promise<R> result = doAfterThis.apply(y);
            // if we've got a promise then wait for it to complete
            result.thenSync((a) -> {
                promise.fulfill(a);
                return null;
            });
            return null;
        });

        return promise;
    }

    public <R> Promise<R> thenSync(Function<T, R> x) {

        if (comodLock) {
            Logger.critical("Tried to chain promise while fulfilling promise: " + this, Tag.POTENTIAL_BUG);
            throw new RuntimeException("Tried to chain promise while fulfilling promise: " + this);
        }

        Promise<R> promise = new Promise<>();

        // instantly resolve
        if (this.fulfilled) {
            promise.fulfill(x.apply(value));
            return promise;
        }

        this.callbacks.add(y -> {
            R res = x.apply(y);
            promise.fulfill(res);
            return null;
        });

        return promise;
    }

    public Promise<T> thenSyncConsumer(Consumer<T> x) {
        return thenSync(y -> {
            x.accept(y);
            return y;
        });
    }

    public static Promise<Void> gather(Promise<?> ... promises) {
        return gather(List.of(promises));
    }

    public static Promise<Void> gather(Stream<Promise<?>> promises) {
        return gather(promises.toList());
    }

    public static Promise<Void> gather(List<Promise<?>> promises) {
        Promise<Void> result = new Promise<>();

        // if there are no promises to wait for, return resolved promise
        if (promises.isEmpty()) {
            result.fulfill(null);
            return result;
        }

        List<Promise<?>> copy = new ArrayList<>(promises);

        for (Promise<?> promise : promises) {
            promise.thenSync(x -> {
                copy.remove(promise);
                if (copy.isEmpty()) {
                    result.fulfill(null);
                }
                return null;
            });
        }

        return result;
    }

    @Override
    public String toString() {
        return "<Promise@" + hashCode() + " " + (fulfilled ? "fulfilled" : "pending") + ">";
    }
}
