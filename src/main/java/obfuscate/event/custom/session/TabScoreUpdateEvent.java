package obfuscate.event.custom.session;

import obfuscate.event.custom.player.TabNameUpdateEvent;
import obfuscate.game.player.StrikePlayer;

public class TabScoreUpdateEvent extends TabNameUpdateEvent {
    private String scoreText;
    private StrikePlayer player;

    public TabScoreUpdateEvent(String scoreText, String name, StrikePlayer player) {
        super(name, player);
        this.scoreText = scoreText;
        this.player = player;
    }

    public String getScoreText() {
        return scoreText;
    }

    public StrikePlayer getPlayer() {
        return player;
    }

}
