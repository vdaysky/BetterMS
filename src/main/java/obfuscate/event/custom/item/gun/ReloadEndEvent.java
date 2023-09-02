package obfuscate.event.custom.item.gun;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Gun;

public class ReloadEndEvent extends CustomEvent {

    private final Game _game;
    private final Gun _gun;
    private final StrikePlayer _holder;
    private final boolean _instant;
    private final ReloadEndReason _reason;
    private final boolean _silent;

    public ReloadEndEvent(Game game, Gun gun, StrikePlayer holder, ReloadEndReason reason, boolean instant, boolean silent) {
        _game = game;
        _gun = gun;
        _holder = holder;
        _instant = instant;
        _reason = reason;
        _silent = silent;

    }

    public ReloadEndReason getReason() {
        return _reason;
    }

    public Game getGame() {
        return _game;
    }

    public Gun getGun() {
        return _gun;
    }

    public StrikePlayer getHolder() {
        return _holder;
    }

    public boolean isInstant() {
        return _instant;
    }

    public boolean isSilent() {
        return _silent;
    }

    public enum ReloadEndReason
    {
        SUCCESS,
        DROP,
        SWITCH,
        DEATH,
        ROUND_END
    }
}


