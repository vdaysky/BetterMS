package obfuscate.game.core.plugins;

import obfuscate.game.config.ConfigField;
import obfuscate.game.core.traits.SharedGameContext;
import obfuscate.gamemode.Competitive;


public class CompetitivePlugin implements IPlugin<Competitive> {


    private Competitive _game;
    private SharedGameContext _ctx;

    @Override
    public void preInit(Competitive instance) {
        this._game = instance;
        this._ctx = instance.getSharedContext();

        var config = instance.getConfig();

        config.setValue(
                instance,
                ConfigField.MAX_PLAYERS,
                10
        );

        config.setValue(
                instance,
                ConfigField.MIN_PLAYERS,
                10
        );

        config.setValue(
                instance,
                ConfigField.CREATE_NEW_ON_COMPLETION,
                0
        );
    }
}

