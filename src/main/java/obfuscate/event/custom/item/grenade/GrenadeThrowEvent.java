package obfuscate.event.custom.item.grenade;

import obfuscate.event.custom.player.PlayerUseItemEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.utility.grenade.Grenade;
import org.bukkit.entity.Entity;

public class GrenadeThrowEvent extends PlayerUseItemEvent
{
    private Entity entity;
    private boolean wasLeftClick;

    public GrenadeThrowEvent(Game Game, Grenade grenade, StrikePlayer thrower, boolean wasLeftClick) {
        super(thrower, Game, grenade);
        this.wasLeftClick = wasLeftClick;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean wasLeftClick() {
        return wasLeftClick;
    }
}

