package obfuscate.network.models.schemas;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.PluginHolder;
import obfuscate.event.custom.PluginManager;
import obfuscate.event.custom.network.ModelEvent;
import obfuscate.game.config.GameConfiguration;
import obfuscate.game.core.GameSession;
import obfuscate.game.core.plugins.IPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.gamemode.registry.GameMode;
import obfuscate.util.serialize.load.LoadableMap;
import obfuscate.util.serialize.load.SyncableObject;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Model;
import obfuscate.team.InGameTeamData;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Model(name = "game")
public class GameData extends SyncableObject implements PluginHolder<Competitive> {

    @Loadable(field = "map")
    private NamedMap map;

    @Loadable(field = "mode")
    private GameMode mode;

    @Loadable(field = "config", explicit = true)
    private GameConfiguration config;

    @Loadable(field = "team_a")
    private InGameTeamData teamA;

    @Loadable(field = "team_b")
    private InGameTeamData teamB;

    @Loadable(field = "blacklist")
    private List<StrikePlayer> blacklist;

    @Loadable(field = "whitelist")
    private List<StrikePlayer> whitelist;

    @Loadable(field = "sessions", key = "_holder")
    protected LoadableMap<StrikePlayer, GameSession> sessions;


    // this is actually quite interesting, this field will be updated every time game
    // instance updates, but it will only construct plugin objects once.
    @Loadable(field = "plugins")
    private ArrayList<String> plugins;

    private final List<IPlugin<Competitive>> pluginInstances = new ArrayList<>();

    @Override
    public List<IPlugin<Competitive>> getPlugins() {
        return pluginInstances;
    }

    public <T extends IPlugin<Competitive>> @Nullable T findPlugin(Class<T> pluginClass) {
        return (T) pluginInstances.stream().filter((x)->x.getClass().equals(pluginClass)).findFirst().orElse(null);
    }

    public List<StrikePlayer> getWhitelist() {
        return whitelist;
    }

    public GameData() {
        this.onFullLoad.thenSync(
            (x)-> {
                for (String plugin : plugins) {
                    Class<?> pluginClass = PluginManager.getPluginClass(plugin);

                    if (pluginClass == null) {
                        MsdmPlugin.logger().warning("No plugin found: " + plugin);
                        continue;
                    }

                    try {
                        pluginInstances.add((IPlugin<Competitive>) pluginClass.getDeclaredConstructor().newInstance());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                return null;
            }
        );
    }

    public GameMode getMode() {
        return mode;
    }

    @Override
    public Class<? extends ModelEvent<? extends SyncableObject>> getFulfilledEvent() {
        return null;
    }

    public GameConfiguration getConfig() {
        return config;
    }

    public InGameTeamData getTeamA() {
        return teamA;
    }

    public InGameTeamData getTeamB() {
        return teamB;
    }

    public String getMapCodeName() {
        return map.getName();
    }

    public List<StrikePlayer> getBlacklistedPlayers() {
        return blacklist;
    }
}
