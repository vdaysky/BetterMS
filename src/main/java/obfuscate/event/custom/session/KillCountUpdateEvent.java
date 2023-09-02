package obfuscate.event.custom.session;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class KillCountUpdateEvent extends CustomEvent {

    private int kills;
    private StrikePlayer player;
    private Game Game;

    public KillCountUpdateEvent(int kills, StrikePlayer player, Game Game) {
        this.kills = kills;
        this.player = player;
        this.Game = Game;
    }

    public int getKills() {
        return kills;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

    public Game getGame() {
        return Game;
    }
}
