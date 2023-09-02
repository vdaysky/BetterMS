package obfuscate.game.core.plugins;

@Plugin
public interface IPlugin<T> {
    void preInit(T instance);
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
