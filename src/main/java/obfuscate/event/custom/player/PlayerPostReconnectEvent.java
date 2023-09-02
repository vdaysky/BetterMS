package obfuscate.event.custom.player;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;

public class PlayerPostReconnectEvent extends PlayerPostGameJoinEvent
{
    public PlayerPostReconnectEvent(Game Game, StrikePlayer player, boolean spectates) {
        super(Game, player, spectates);
    }
}
