package obfuscate.event.custom.player;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class PlayerReconnectEvent extends PlayerJoinGameEvent
{
    public PlayerReconnectEvent(Game game, StrikePlayer player) {
        super(game, player, false);
    }
}
