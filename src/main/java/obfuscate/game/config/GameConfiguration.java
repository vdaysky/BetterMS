package obfuscate.game.config;

import obfuscate.event.custom.config.ConfigFieldChangeEvent;
import obfuscate.game.core.Game;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.util.serialize.load.Loadable;
import obfuscate.util.serialize.load.Struct;

import java.util.HashMap;

/** WrappedConfig is exactly like independent config,
 *  but it triggers events on field change and passes them to game. */
public class GameConfiguration extends Struct {

    @Loadable(field = "overrides", merge = true)
    private HashMap<String, Long> _overrides = new HashMap<>();

    // overrides made by plugin itself, have lower priority than ones set by server
    private final HashMap<String, Integer> _internalOverrides = new HashMap<>();

    public GameConfiguration() {
    }

    public ConfigValue getValue(ConfigField field) {
        if (_overrides.containsKey(field.field_name.toLowerCase())) {
            Long lVal = _overrides.get(field.field_name.toLowerCase());
            return new ConfigValue(lVal.intValue());
        }
        if (_internalOverrides.containsKey(field.field_name.toLowerCase())) {
            return new ConfigValue(_internalOverrides.get(field.field_name.toLowerCase()));
        }
        return new ConfigValue(field.default_value);
    }

    public void setValue(Game game, ConfigField field, Integer value) {
        Integer old = getValue(field).val();
        _internalOverrides.put(field.field_name.toLowerCase(), value);
        new ConfigFieldChangeEvent(game, field, old, value).trigger();
    }

    public Integer getDuration(GeneralGameStage stage) {
        return getValue(ConfigField.getField(stage.getNiceName() + "-duration")).val();
    }

}
