package obfuscate.util.serialize.load;

import obfuscate.util.Promise;

public class ObjectLoadFuture<T extends Struct> {

    private final T object;

    private final Promise<?> promise;

    public ObjectLoadFuture(T object, Promise<?> promise) {
        this.object = object;
        this.promise = promise;
    }

    public T getObject() {
        return object;
    }

    public Promise<T> getPromise() {
        return promise.thenSync(x -> object);
    }
}
