package obfuscate.game.core.traits;

import obfuscate.event.custom.gamestate.FreezeTimeEndEvent;
import obfuscate.event.custom.gamestate.GameEndEvent;
import obfuscate.event.custom.gamestate.RoundResetEvent;
import obfuscate.event.custom.gamestate.WarmUpEndEvent;
import obfuscate.game.core.Game;
import obfuscate.game.state.GeneralGameStage;

public class DefusalStateUpdater implements GameStateUpdater {
    @Override
    public void update(Game game) {

        var stateDurationLeft = game.getGameStateDuration();
        var activeState = game.getGameState();

        // Freeze time ticking
        if (stateDurationLeft > 0 && stateDurationLeft <= 10 && activeState.isFreezeTime()) {
            game.getSoundManager().tick().vol(0.5f).play();
        }

        if (stateDurationLeft == 0) {

            if (activeState.isWarmup()) {
                game.setGameState(GeneralGameStage.FREEZE_TIME);

                // event
                new WarmUpEndEvent(game).trigger();

            } else if (activeState.isFreezeTime()) {
                game.getSoundManager().harp().vol(0.5f).pitch(2).play();
                game.setGameState(GeneralGameStage.LIVE);

                // event
                new FreezeTimeEndEvent(game).trigger();

            } else if (activeState.isLive()) {
                game.setRoundEndState();
            } else if (activeState.isRoundEnd()) {
                //event
                new RoundResetEvent(game).trigger();

                if (game.hasNextPause()) {
                    game.startNextPause();
                } else {
                    game.setGameState(GeneralGameStage.FREEZE_TIME);
                }

            } else if (activeState.getGeneralStage() == GeneralGameStage.GAME_END) {
                //event
                new GameEndEvent(game).trigger();
            } else if (activeState.getGeneralStage() == GeneralGameStage.PAUSED) {
                if (game.hasNextPause()) {
                    game.startNextPause();
                } else {
                    game.setGameState(GeneralGameStage.FREEZE_TIME);
                }
            }
        }
    }
}
