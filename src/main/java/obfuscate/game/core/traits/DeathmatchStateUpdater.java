package obfuscate.game.core.traits;

import obfuscate.event.custom.gamestate.WarmUpEndEvent;
import obfuscate.game.core.Game;
import obfuscate.game.state.GameStateInstance;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.game.state.StateTag;

public class DeathmatchStateUpdater implements GameStateUpdater {
    @Override
    public void update(Game game) {
        if (game.getGameState().isWarmup() && game.getGameStateDuration() == 0) {
            new WarmUpEndEvent(game).trigger();

            var stage = new GameStateInstance(
                    "DeathMatch",
                    GeneralGameStage.LIVE,
                    StateTag.RESPAWNABLE,
                    StateTag.JOINABLE,
                    StateTag.TICKABLE,
                    StateTag.DAMAGE_ALLOWED,
                    StateTag.CAN_INTERACT,
                    StateTag.CAN_MOVE
            );
            game.setGameState(stage, 30 * 60);
        }
        else if (game.getGameStateDuration() == 0 && game.getGameState().isLive()) {
            game.endGame(game.getTeamA(), game.getTeamB());
        }
    }
}
