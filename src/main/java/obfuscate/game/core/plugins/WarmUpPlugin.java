package obfuscate.game.core.plugins;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.game.GameInitEvent;
import obfuscate.event.custom.game.GameStartCancelEvent;
import obfuscate.event.custom.gamestate.WarmUpEndEvent;
import obfuscate.event.custom.player.PlayerPostGameJoinEvent;
import obfuscate.event.custom.player.PlayerPostLeaveGameEvent;
import obfuscate.event.custom.ready.PlayerReadyEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.IGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.*;
import obfuscate.gamemode.Competitive;
import obfuscate.message.MsgSender;
import obfuscate.util.chat.C;
import obfuscate.util.time.Task;
import org.bukkit.ChatColor;

import static obfuscate.event.LocalPriority.PRE;

/** When game starts, WarmUp state is injected.
 * After that plugin will handle events related to whether game can start, like player join or leave.
 * Once game can start, countdown will begin. If game start condition is not met after countdown runs out,
 * game start will be cancelled.
 */
public class WarmUpPlugin implements IPlugin<Competitive> {

    private Task readyHintTask = null;
    private boolean isReady = false;

    @Override
    public void preInit(Competitive instance) {
        // this plugin will handle starting the game.
        // there are certain conditions to be met in order to start the game
        instance.getSharedContext().defersGameStart(true);
        instance.setRound(-1); // round is -1 meaning game is not started yet
        instance.setGameState(GeneralGameStage.WARM_UP);
        instance.setLeftDuration(-1);

        new Task(() -> {

            if (instance.isInProgress()) {
                return;
            }

            if (instance.getPhaseSecondsLeft() != -1) {
                return;
            }

            var minPlayers = instance.get(ConfigField.MIN_PLAYERS);

            instance.broadcastChat(MsgSender.NONE, "");

            instance.broadcastChat(
                    MsgSender.GAME,
                    C.cGray + "Waiting for players to join."
            );

            instance.broadcastChat(
                    MsgSender.GAME,
                    C.cGray + "Game requires at least " + C.cYellow + minPlayers + C.cGray + " players to start."
            );

            instance.broadcastChat(MsgSender.NONE, "");

        }, 0, 20 * 60).run();
    }

    private void setReady(IGame game, boolean ready) {
        MsdmPlugin.info("Setting ready state to " + ready + " in game " + game.getId().getObjId());

        if (game.isInProgress()) {
            MsdmPlugin.logger().warning("Cannot set ready state when game is already in progress!");
            return;
        }

        if (isReady == ready) {
            return;
        }

        isReady = ready;

        if (!isReady) { // cancel countdown
            MsdmPlugin.info("Cancel game countdown");
            game.setGameState(GeneralGameStage.WARM_UP);
            game.getGameState().removeTag(StateTag.TICKABLE);
            game.setLeftDuration(-1);
            game.broadcastChat(MsgSender.NONE, "");
            game.broadcastChat(MsgSender.GAME, ChatColor.RED + "The game has been cancelled!");
            game.broadcastChat(MsgSender.NONE, "");
        } else { // start countdown
            MsdmPlugin.info("Start game countdown");
            game.getGameState().setTag(StateTag.TICKABLE);
            int warmUpDuration = game.getConfig().getDuration(GeneralGameStage.WARM_UP);
            game.setLeftDuration(warmUpDuration);

            game.broadcastChat(
                    MsgSender.NONE,
                    ""
            );

            game.broadcastChat(
                    MsgSender.GAME,
                    C.cWhite + "Starting the game in " + C.cYellow + 60 + C.cWhite + " seconds"
            );

            game.broadcastChat(
                    MsgSender.NONE,
                    ""
            );
        }
    }

    /** This handler will start the game if it is ready and warmup ends */
    @LocalEvent(priority = PRE)
    private void handleGameStateEnd(WarmUpEndEvent e)
    {
        // this is the place to cancel game start if conditions are not met
        // if event is forced, we can't object to it here,
        // because state manager does not give a fuck
        // that shit is broken
        if (!e.getGame().isInProgress() && !e.getGame().canStart()) {
            setReady(e.getGame(), false);
            return;
        }

        // cancellability is questionable.
//        if (e.isCancelled())
//            return;

//        GameStateInstance endedState = e.getEndedState();

        // this is weird, warmup gets replaced with warmup. I should fix this
//        if (endedState.getGeneralStage() == GeneralGameStage.WARM_UP && e.getNextState().getGeneralStage() != GeneralGameStage.WARM_UP) {
            e.getGame().setRound(1); // round is 1 meaning game is started
            e.getGame().startGame();
//        }
    }

    @LocalEvent(priority = LocalPriority.PRE_HIGH)
    private void onSecond(TimeEvent e) {
        if (e.getReason() != TimeEvent.UpdateReason.SECOND) {
            return;
        }

        var durationLeft = e.getGame().getPhaseSecondsLeft();


        if (e.getGame().getGameState().isWarmup() && durationLeft == 30) {
            e.getGame().broadcastChat(
                    MsgSender.GAME,
                    C.cWhite + "Starting the game in " + C.cYellow + 30 + C.cWhite + " seconds"
            );
        }

        if (e.getGame().getGameState().isWarmup() && durationLeft == 10) {
            e.getGame().broadcastChat(
                    MsgSender.GAME,
                    C.cWhite + "Starting the game in " + C.cYellow + 10 + C.cWhite + " seconds"
            );
        }

        if (!e.getGame().getGameState().isWarmup()) {
            return;
        }

        if (durationLeft < 0) {
            return;
        }

        if (durationLeft == 0 && e.getGame().canStart()) {
            e.getGame().broadcastTitle(
                    C.cGreen + "Live!",
                    0, 60, 0
            );
            e.getGame().broadcastChat(
                    MsgSender.GAME,
                    C.cGreen + "Game is Live!"
            );
        }

        else if (durationLeft <= 5) {
            e.getGame().broadcastTitle(
                    C.cRed + durationLeft,
                    0, 20, 0
            );
            e.getGame().getSoundManager().tick().vol(0.5f).play();
        }
        else if (durationLeft <= 10) {
            e.getGame().broadcastTitle(C.cYellow + durationLeft);
            e.getGame().getSoundManager().tick().vol(0.5f).play();
        }
    }

    @LocalEvent
    private void cancelStart(GameStartCancelEvent e)
    {
        if (readyHintTask != null)
            readyHintTask.cancel();
    }

    private void printReadyHint(Competitive game)
    {
        game.broadcastChat(MsgSender.GAME, C.cGreen  + "Game will start when everyone is " + C.cYellow + "/ready" + C.cGreen + ".");
        game.broadcastChat(MsgSender.GAME, C.cGreen  + "Not ready:");

        for (StrikePlayer player : game.getNotReadyPlayers())
        {
            game.broadcastChat(MsgSender.GAME, C.cGreen  +"- " + game.getPlayerRoster(player).getTeam().getColor() + player.getName());
        }
    }

    private void startHintLoop(Competitive game)
    {
        if (readyHintTask != null)
            return;

        if (game.isInProgress())
        {
            readyHintTask.cancel();
            readyHintTask = null;
            return;
        }

        readyHintTask = new Task(()->
        {
            if (!game.isInProgress() && !game.getGameState().is(StateTag.TICKABLE))
                printReadyHint(game);
            else {
                readyHintTask.cancel();
                readyHintTask = null;
            }
        }, 0, 1200); // 1 minute

        readyHintTask.run();
    }

    @LocalEvent
    public void onGameStart(GameInitEvent e) {

        // do not inject new game state is game is ready instantly
        if(e.getGame().is(ConfigField.INSTANTLY_READY)) {
            isReady = true;
            e.getGame().startGame();
            return;
        }

//        GameStateInstance gameState = new GameStateInstance("Warm up", GeneralGameStage.WARM_UP);
//
//        MsdmPlugin.info("Injecting warm up state");
//
//        e.getGame().getStateManager().injectStateBeforeCurrent(
//            gameState,
//            -1, // infinite warm up till game ready
//            true
//        );
    }

    /** Start countdown if we can start the game */
    void checkStartConditions(Competitive game)
    {
        MsdmPlugin.info("Check game start conditions: " + game.getId().getObjId());
        if (game.isInProgress()) {
            MsdmPlugin.info("Game is already in progress");
            return;
        }


        // some players are not ready, start hints in case they are fucking stupid
        if (game.readyStateRequiredByConfig() && !game.everyoneReady()) {
            startHintLoop(game);
        }

        setReady(game, game.canStart());
    }

    @LocalEvent
    public void onPlayerReady(PlayerReadyEvent event)
    {
        event.getGame().broadcastChat(MsgSender.GAME, C.cYellow + event.getPlayer().getName() + C.cGreen + " is now ready");
        new Task(()->checkStartConditions((Competitive) event.getGame()), 20).run();
    }

    @LocalEvent(cascade = true, priority = LocalPriority.POST)
    public void onPlayerJoinEvent(PlayerPostGameJoinEvent e) {
        MsdmPlugin.info("[WarmupPlugin] Player joined a game");
        if (!e.getGame().isInProgress()) {
            boolean countDown = e.getGame().getPhaseSecondsLeft() != -1;

            e.getPlayer().sendMessage("");
            if (countDown) {
                e.getPlayer().sendMessage(MsgSender.GAME, "Warm Up in progress, game will be starting soon");
            } else {
                var minPlayers = e.getGame().get(ConfigField.MIN_PLAYERS);

                e.getPlayer().sendMessage(
                        MsgSender.GAME,
                        C.cGray + "Waiting for players to join."
                );
                e.getPlayer().sendMessage(
                        MsgSender.GAME,
                        C.cGray + "Game requires at least " + C.cYellow + minPlayers + C.cGray + " players to start."
                );
            }

            e.getPlayer().sendMessage("");
        }

        checkStartConditions((Competitive) e.getGame());
    }

    @LocalEvent
    public void onPlayerLeaveTeamGame(PlayerPostLeaveGameEvent e) {
        checkStartConditions((Competitive) e.getGame());
    }
}
