package obfuscate.event.custom.config;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;

public class ConfigFieldChangeEvent extends CancellableEvent {
    private Game game;
    private ConfigField field;
    private Integer oldValue;
    private Integer newValue;

    public ConfigFieldChangeEvent(Game game, ConfigField field, Integer oldValue, Integer newValue) {
        this.game = game;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Game getGame() {
        return game;
    }

    public ConfigField getField() {
        return field;
    }

    public Integer getOldValue() {
        return oldValue;
    }

    public Integer getNewValue() {
        return newValue;
    }
}
