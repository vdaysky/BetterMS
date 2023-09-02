package obfuscate.event.custom;

import obfuscate.game.core.plugins.IPlugin;
import obfuscate.gamemode.Competitive;

import java.util.List;

public interface PluginHolder<T> {

    List<IPlugin<Competitive>> getPlugins();

}
