package obfuscate.game.core;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.pause.PauseStartEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.*;
import obfuscate.logging.Logger;
import obfuscate.team.InGameTeamData;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class PausableGame extends ShoppableGame
{
    private Pause _activePause = null;
    private ArrayList<Pause> _pauseQueue = new ArrayList<>();

    private final HashMap<InGameTeamData, Integer> pausesUsed = new HashMap<>();

    /** @return whether game can be set on pause (state is pausable) */
    public boolean canPause() {
        return getGameState().is(StateTag.PAUSABLE);
    }

    public boolean isPaused() {
        return _activePause != null;
    }

    public boolean hasNextPause() {
        return !_pauseQueue.isEmpty();
    }

    public Pause getPauseType()
    {
        return _activePause;
    }

    public void increaseUsedPauseCount(InGameTeamData roster)
    {
        pausesUsed.put(roster, getUsedPauses(roster)+1);
    }

    public int getUsedPauses(InGameTeamData roster)
    {
        pausesUsed.putIfAbsent(roster, 0);
        return pausesUsed.get(roster);
    }

    public boolean hasPauses(InGameTeamData roster)
    {
        return getUsedPauses(roster) < getConfig().getValue(ConfigField.MAX_TAC_TIMEOUTS).val();
    }

    /** used by users to interact with pauses */
    public boolean requestTacPause(InGameTeamData pausingEntry)
    {
        if (!hasPauses(pausingEntry))
            return false;

        increaseUsedPauseCount(pausingEntry);
        this._pauseQueue.add(Pause.TACTICAL);

        Logger.severe("Pauses are not implemented yet!", pausingEntry);

//        if (canPause()) {
//            startNextPause();
//        } else {
//            // find next pausable state to send a message
//            var stateMgr = getStateManager();
//            for (int i = 0; i < stateMgr.getGameStates().size(); i++)
//            {
//                var gState = stateMgr.getGameState(i);
//                if (gState.is(StateTag.PAUSABLE)) {
//                    broadcast(MsgSender.GAME, "Game will be paused during next " + gState.getName(), MsgType.CHAT);
//                    break;
//                }
//            }
//        }
        return true;
    }

    /** replace queue with technical pause */
    public void setTecPause() {
        _pauseQueue.clear();
        _pauseQueue.add(Pause.TECHNICAL);

        // start immediately if possible
        if (canPause()) {
            startNextPause();
        }
    }

    /** Cancel all pauses */
    public void unpause() {

        // usually I don't like those checks, but I don't want
        // to make it skip any state like skip command
        if (!isPaused()) {
            return;
        }

        _activePause = null;
        _pauseQueue.clear();
        setLeftDuration(0);
//        getStateManager().goToNextState(); // untested
    }

    /** Replace current state with pause state. Does not make any checks, be careful where it is called. */
    public boolean startNextPause()
    {
        Logger.info("Attempting to start next pause", this);
        if (!hasNextPause()) {
            Logger.info("There are no pauses", this);
            return false;
        }

        // pause to process
        _activePause = _pauseQueue.remove(0);

        Logger.info("Will start pause" + _activePause, this);

        new PauseStartEvent(_activePause, this).trigger();

        GameStateInstance pauseState;
        Integer sDuration;

        if (_activePause == Pause.TACTICAL)
        {
            pauseState = new GameStateInstance("Tactical Pause",
                    GeneralGameStage.PAUSED,
                    StateTag.TICKABLE,
                    StateTag.JOINABLE
            );

            sDuration = getConfig().getValue(ConfigField.TAC_DURATION).val();
        }
        else
        {
            pauseState = new GameStateInstance("Technical Pause",
                    GeneralGameStage.PAUSED,
                    StateTag.JOINABLE
            );

            sDuration = -1;
        }

        setGameState(pauseState);
        setLeftDuration(sDuration);
        return true;
    }

    public boolean canMove(StrikePlayer player)
    {
        return super.canMove(player) && !isPaused();
    }


//    @LocalEvent
//    protected void checkIfPauseEnded(GameStateEndEvent e)
//    {
//        if (e.getEndedState().getGeneralStage() != GeneralGameStage.PAUSED)
//            return;
//
//        // no more pauses, do not cancel event, let state change
//        if (!hasNextPause()) {
//            new PauseEndEvent(_activePause, this).trigger();
//            _activePause = null;
//            return;
//        }
//
//        // prevent end of pause state because we have more
//        // todo: insert multiple gamestate instances for each pause
//        MsdmPlugin.highlight("Cancel game state end event because pause has to be extended");
//        e.setCancelled(true);
//        startNextPause();
//    }

    public int getPausesLeft(InGameTeamData key) {
        return getConfig().getValue(ConfigField.MAX_TAC_TIMEOUTS).val() - pausesUsed.get(key);
    }
}
