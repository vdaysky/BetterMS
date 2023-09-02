package obfuscate.event.custom.session;

import obfuscate.game.core.Game;
import obfuscate.game.core.PlayerStatus;
import obfuscate.game.player.StrikePlayer;

public class PlayerPostStatusChangeEvent extends PlayerStatusChangeEvent {

    public PlayerPostStatusChangeEvent(Game Game, StrikePlayer player, PlayerStatus state) {
        super(Game, player, state);
    }
}
