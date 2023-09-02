package obfuscate.event.custom.item.objective;

import obfuscate.event.custom.player.PlayerUseItemEvent;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.objective.Bomb;

public class PlayerAttemptPlantEvent extends PlayerUseItemEvent {

    public PlayerAttemptPlantEvent(StrikePlayer player, Game game, Bomb item) {
        super(player, game, item);
    }
}
