package obfuscate.event.custom.session;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class KdUpdateEvent extends CustomEvent {
    private int kills;
    private int deaths;
    private StrikePlayer player;
    private Game Game;

    public KdUpdateEvent(int kills, int deaths, StrikePlayer player, Game Game) {
        this.kills = kills;
        this.player = player;
        this.Game = Game;
        this.deaths = deaths;
    }

    public int getKills() {
        return kills;
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
