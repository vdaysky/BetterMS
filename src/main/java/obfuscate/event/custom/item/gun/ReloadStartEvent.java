package obfuscate.event.custom.item.gun;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Gun;

public class ReloadStartEvent extends CustomEvent
{
    private final Game game;
    private final Gun gun;
    private final StrikePlayer holder;
    private final boolean instant;

    public ReloadStartEvent(Game game, Gun gun, StrikePlayer holder, boolean instant)
    {
        this.game = game;
        this.gun = gun;
        this.holder = holder;
        this.instant = instant;
    }

    public Game getGame() {
        return game;
    }

    public Gun getGun() {
        return gun;
    }

    public StrikePlayer getHolder() {
        return holder;
    }

    public boolean isInstant() {
        return instant;
    }
}
