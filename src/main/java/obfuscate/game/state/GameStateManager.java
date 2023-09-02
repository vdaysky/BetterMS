//package HoldMyOrangeJuice.game.state;
//
//import HoldMyOrangeJuice.MsdmPlugin;
//import HoldMyOrangeJuice.event.custom.gamestate.GameStateChangeEvent;
//import HoldMyOrangeJuice.event.custom.gamestate.GameStateEndEvent;
//import HoldMyOrangeJuice.event.custom.gamestate.GameStateStartEvent;
//import HoldMyOrangeJuice.event.custom.gamestate.StateChangeReason;
//import HoldMyOrangeJuice.game.core.Game;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//
//public class GameStateManager
//{
//    private final Game _game;
//    private Integer phaseSecondsLeft;
//
//    // Base state sequence, defines states that will be used throughout round
//    private final List<GameStateInstance> baseStateSequence;
//
//    // actual state queue for the round, can be modified
//    private List<GameStateInstance> currentQueue = new ArrayList<>();
//
//    // location within the stack of states,
//    // defines what next state would be
//    private Integer currentIndex = 0;
//
//    public GameStateManager(Game game, List<GameStateInstance> gameStates) {
//
//        this.baseStateSequence = gameStates;
//
//        // initialize queue with base states
//        this.currentQueue.addAll(gameStates);
//
//        _game = game;
//        phaseSecondsLeft = getConfigStateDuration();
//    }
//
//    public List<GameStateInstance> getGameStates()
//    {
//        return currentQueue;
//    }
//
//    /**
//     * @return active state
//     * @see GameStateInstance
//     * */
//    public GameStateInstance getActiveState()
//    {
//        return currentQueue.get(currentIndex);
//    }
//
//    /** @return total duration of current state in seconds */
//    public Integer getConfigStateDuration()
//    {
//        return _game.getConfig().getDuration(getActiveState().getGeneralStage());
//    }
//
//    /** @return duration left of current state in seconds */
//    public Integer getDurationLeft() {
//        return phaseSecondsLeft;
//    }
//
//    /** Update method should be called every second if state is tickable */
//    public void update() {
//        if (phaseSecondsLeft > 0) {
//            phaseSecondsLeft--;
//        } else {
//            goToNextState();
//        }
//    }
//
//    private boolean attemptToChangeState(GameStateInstance nextState) {
//        GameStateInstance currentState = getActiveState();
//
//        MsdmPlugin.logger().info("Trying ot replace state " + currentState.getGeneralStage() + " with " + nextState.getGeneralStage() + " in game " + _game.getId().getObjId());
//
//        // if event is forced it won't be cancelled
//        boolean cancelled = new GameStateEndEvent(
//                _game,
//                currentState,
//                nextState
//        ).triggerSync();
//
//        if (cancelled) {
//            MsdmPlugin.logger().info("State end Event was cancelled" + " in game " + _game.getId().getObjId());
//            return false;
//        }
//
//
//        cancelled = new GameStateChangeEvent(_game, currentState, nextState).triggerSync();
//
//        if (cancelled) {
//            MsdmPlugin.logger().info("State change Event was cancelled" + " in game " + _game.getId().getObjId());
//            return false;
//        }
//        return true;
//    }
//
//    private void confirmStateChange(GameStateInstance nextState) {
//
//
//        new GameStateStartEvent(_game, nextState, StateChangeReason.SET).trigger();
//    }
//
//    /** triggers event and sets state of game it event wasn't cancelled. */
//    public boolean goToNextState() {
//
//        GameStateInstance nextState = getNextState();
//        boolean looped = false;
//
//        if (nextState == null) {
//            nextState = baseStateSequence.get(0);
//            looped = true;
//        }
//
//        if (!attemptToChangeState(nextState)) {
//            return false;
//        }
//
//        MsdmPlugin.logger().info("Incrementing current state index: " + currentIndex + " + 1" + " in game " + _game.getId().getObjId());
//
//
//        if (looped) {
//            MsdmPlugin.logger().info("Current index is equal to size of game states, resetting" + " in game " + _game.getId().getObjId());
//            currentIndex = 0;
//            currentQueue = new ArrayList<>(baseStateSequence);
//            MsdmPlugin.logger().info("Current queue: " + currentQueue);
//        } else {
//            currentIndex += 1;
//        }
//
//        int duration = getConfigStateDuration();
//        MsdmPlugin.logger().info("Set new state duration: " + duration);
//        setLeftDuration(duration);
//
//        confirmStateChange(nextState);
//        return true;
//    }
//
//    private @Nullable GameStateInstance getNextState() {
//        int nextIdx = currentIndex + 1;
//        if (nextIdx >= getGameStates().size()) {
//            return null;
//        }
//        return getGameStates().get(nextIdx);
//    }
//
//    /** Whether next state exists  */
//    public boolean hasNext()
//    {
//        return currentIndex + 1 != getGameStates().size();
//    }
//
//    /** sets duration of active state in seconds */
//    public void setLeftDuration(int seconds)
//    {
//       phaseSecondsLeft = seconds;
//    }
//
//    public void injectStateAfterCurrent(GameStateInstance gameState, Integer duration) {
//        MsdmPlugin.info("Injecting state " + gameState.getGeneralStage() + " after current state " + getActiveState().getGeneralStage() + " in game " + _game.getId().getObjId());
//
//        // add one place at the end
//        getGameStates().add(null);
//
//        for (int i = getGameStates().size() - 1; i > currentIndex + 1; i--) {
//            getGameStates().set(i, getGameStates().get(i - 1));
//        }
//        getGameStates().set(currentIndex + 1, gameState);
//        setLeftDuration(duration == null ? getConfigStateDuration() : duration);
//    }
//
//    public void resetLoop() {
//        currentIndex = 0;
//        currentQueue = new ArrayList<>(baseStateSequence);
//    }
//
//    public void injectStateBeforeCurrent(GameStateInstance gameState, int i, boolean notused) {
//
//        if (!attemptToChangeState(gameState)) {
//            MsdmPlugin.info("injectStateBeforeCurrent: attemptToChangeState returned false" + " in game " + _game.getId().getObjId());
//            return;
//        }
//
//        // add one place at the end
//        getGameStates().add(null);
//
//        MsdmPlugin.info("Injecting state " + gameState.getGeneralStage() + " before current state " + getActiveState().getGeneralStage() + " in game " + _game.getId().getObjId());
//        for (int j = getGameStates().size() - 1; j > currentIndex; j--) {
//            getGameStates().set(j, getGameStates().get(j - 1));
//        }
//
//        getGameStates().set(currentIndex, gameState);
//        setLeftDuration(i);
//
//        confirmStateChange(gameState);
//    }
//}
