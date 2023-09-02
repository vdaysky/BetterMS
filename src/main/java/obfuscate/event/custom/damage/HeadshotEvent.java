package obfuscate.event.custom.damage;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class HeadshotEvent extends CustomEvent {

    private final Game Game;
    private final StrikePlayer damagee;
    private final StrikePlayer damager;

    public HeadshotEvent(Game Game, StrikePlayer damagee, StrikePlayer damager) {
        this.Game = Game;
        this.damagee = damagee;
        this.damager = damager;
    }

    public Game getGame() {
        return Game;
    }

    public StrikePlayer getDamagee() {
        return damagee;
    }

    public StrikePlayer getDamager() {
        return damager;
    }
}
