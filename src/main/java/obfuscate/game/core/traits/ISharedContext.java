package obfuscate.game.core.traits;

import obfuscate.game.state.GameStateInstance;

import java.util.List;

/** Mutable piece of configuration designed for complex values that can be touched from outside (e.g. by game plugin) */
public interface ISharedContext {
    List<GameStateInstance> getStages();

    void setGameStages(List<GameStateInstance> stages);

    String getModeName();

    void setModeName(String name);

    boolean defersGameStart();

    void defersGameStart(boolean defers);

    SidebarUpdater getSidebarUpdater();

    void setSidebarUpdater(SidebarUpdater updater);

    GameStateUpdater getGameStateUpdater();

    void setGameStateUpdater(GameStateUpdater updater);
}
