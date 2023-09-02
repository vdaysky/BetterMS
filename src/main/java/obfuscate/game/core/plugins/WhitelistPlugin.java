package obfuscate.game.core.plugins;

import obfuscate.game.core.traits.SharedGameContext;
import obfuscate.gamemode.Competitive;


public class WhitelistPlugin implements IPlugin<Competitive> {


    private Competitive _game;
    private SharedGameContext _ctx;

    @Override
    public void preInit(Competitive instance) {
        this._game = instance;
        this._ctx = instance.getSharedContext();
    }
}

