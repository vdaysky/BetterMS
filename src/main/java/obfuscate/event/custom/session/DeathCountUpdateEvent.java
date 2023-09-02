package obfuscate.event.custom.session;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class DeathCountUpdateEvent extends CustomEvent {

    private int deaths;
    private StrikePlayer player;
    private Game Game;

    public DeathCountUpdateEvent(int kills, StrikePlayer player, Game Game) {
        this.deaths = kills;
        this.player = player;
        this.Game = Game;
    }

    public int getDeaths() {
        return deaths;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public Game getGame() {
        return Game;
    }
}
