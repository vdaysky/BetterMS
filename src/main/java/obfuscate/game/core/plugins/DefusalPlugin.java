package obfuscate.game.core.plugins;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.item.ItemPickUpEvent;
import obfuscate.event.custom.item.ItemPostDropEvent;
import obfuscate.event.custom.item.objective.PlayerAttemptPlantEvent;
import obfuscate.event.custom.objective.BombDefuseEvent;
import obfuscate.event.custom.objective.BombPlantEvent;
import obfuscate.event.custom.round.RoundStartEvent;
import obfuscate.event.custom.round.RoundWinEvent;
import obfuscate.event.custom.time.TimeEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.IGame;
import obfuscate.game.core.traits.SharedGameContext;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.GameStateInstance;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.gamemode.Competitive;
import obfuscate.mechanic.item.objective.Bomb;
import obfuscate.mechanic.item.objective.Compass;
import obfuscate.mechanic.item.objective.DefusalKit;
import obfuscate.message.MsgSender;
import obfuscate.team.StrikeTeam;
import obfuscate.util.chat.C;

import java.util.Objects;

public class DefusalPlugin implements IPlugin<Competitive> {

    private SharedGameContext ctx;

    @Override
    public void preInit(Competitive instance) {
        ctx = instance.getSharedContext();
    }

    public void updateBomb(Competitive game)
    {
        if (ctx.getBomb() != null && ctx.getBomb().isPlanted() && !ctx.getBomb().isExploded() && !ctx.getBomb().isDefused() && !ctx.getBomb().isBeingPlanted())
        {
            for (StrikePlayer ct : game.getPlayers(StrikeTeam.CT, true))
            {
                if (ctx.getBomb().canSee(ct))
                {
                    ctx.setDefuser(ct);
                    ctx.getBomb().startDefuse(game, ct);
                    break;
                }
            }
        }
    }

    public void startBombPlant(Competitive game, StrikePlayer player)
    {
        if (ctx.getBomb() == null)return;

        if (!ctx.getBomb().canPlant(game, player))
            return;

        if (Objects.equals(game.getGameMap().getSiteName(player.getLocation()), "B") && game.is(ConfigField.BLOCK_B_SITE)) {
            player.sendMessage(MsgSender.GAME, C.cRed + "You can't plant on B site!");
            return;
        }

        if (ctx.getBomb() != null && player.getHeldItem(game) == ctx.getBomb())
        {
            // planter is never reset to null # i removed planter, planter = holder if bombIsBeingPlanted now
            ctx.getBomb().startPlant(game, player);
        }
    }

    @LocalEvent
    public void onDefuse(BombDefuseEvent e) {
        IGame game = e.getGame();

        if (!game.isInProgress() || game.isEnded())
            return;

        // bomb was defused after team won, looks like impossible situation
        if (game.getCurrentRoundWinner() != null) {
            return;
        }

        game.winRound(StrikeTeam.CT, RoundWinEvent.Reason.OBJECTIVE);

        // we don't want after-round defuse to mess up state chain
        if (game.getGameState().getGeneralStage() == GeneralGameStage.BOMB_PLANT) {
            game.setRoundEndState();
        }
    }

    @LocalEvent
    public void pickupEvent(ItemPickUpEvent e)
    {
        StrikeTeam team = e.getGame().getPlayerRoster(e.getPlayer()).getTeam();

        // prevent CT's picking up the bomb
        if (team != StrikeTeam.T && e.getItem() instanceof Bomb)
            e.setCancelled(true);

        // prevent T's picking up kits
        if (team != StrikeTeam.CT && e.getItem() instanceof DefusalKit)
            e.setCancelled(true);

        // keep track of who has the bomb
        if (team == StrikeTeam.T && e.getItem() instanceof Bomb) {
            ctx.setBombCarry(e.getPlayer());

            // Bomb Message
            for (StrikePlayer player : e.getGame().getOnline(e.getGame().getRoster(StrikeTeam.T))) {
                String text = e.getPlayer().getName() + " picked up the Bomb";
                if (player == e.getPlayer()) {
                    text = "You picked up the Bomb";
                }
                player.sendTitle("", text, 10, 40, 20);
                player.sendMessage(C.cGold + C.Bold + text + "!");
            }
        }
    }

    @LocalEvent
    private void onBombDrop(ItemPostDropEvent e) {
        // keep track of who has the bomb
        if (e.getHolder() == ctx.getBombCarry() && e.getItem() == ctx.getBomb()) {
            ctx.setBombCarry(null);
            e.getGame().getTRadio().bombDrop();
            e.getGame().getSoundManager().dropBomb().forTeam(StrikeTeam.T).play();

            new Compass().giveToPlayer(e.getGame(), e.getHolder(),  false);
        }
    }

    @LocalEvent(priority = LocalPriority.PRE)
    private void onRoundStart(RoundStartEvent e)
    {
        MsdmPlugin.highlight("Round start event");

        // remove bomb block
        if (ctx.getBomb() != null) {
            if (ctx.getBomb().isBeingPlanted()) {
                ctx.getBomb().cancelPlant(e.getGame(), ctx.getBombCarry());
            }

            ctx.getBomb().remove(false);
            ctx.getBomb().stopDefuse(e.getGame(), ctx.getDefuser());
        }

        // replace the bomb with compass for bomb holder from previous round
        if (ctx.getBombCarry() != null) {
            ctx.getBombCarry().getInventory(e.getGame()).setItem(ctx.getBomb().getSlot(), null);
        }

        // create new bomb
        // effectively resets bomb state
        ctx.setBomb(new Bomb());

        // give bomb
        StrikePlayer randomT = ((Competitive)e.getGame()).getRandomInGameMember(StrikeTeam.T);
        MsdmPlugin.highlight("Random T to get the bomb: " + randomT);
        ctx.setBombCarry(randomT);

        if (ctx.getBombCarry() != null) {
            MsdmPlugin.highlight("Giving bomb to " + ctx.getBombCarry());
            ctx.getBomb().giveToPlayer(e.getGame(), ctx.getBombCarry(), true);
        }
        else
        {
            System.out.println("[WARN] could not find T player to give bomb to");
        }
    }


    @LocalEvent
    private void playBombDropSound(ItemPostDropEvent e) {
        if (e.getItem() instanceof Bomb) {
            e.getGame().getSoundManager().dropBomb().forTeam(StrikeTeam.T).play();
        }
    }

    @LocalEvent
    private void attemptPlant(PlayerAttemptPlantEvent e) {
        startBombPlant((Competitive) e.getGame(), e.getPlayer());
    }

    @LocalEvent
    public void tick(TimeEvent e)
    {
        if (e.getReason() != TimeEvent.UpdateReason.TICK)return;
        updateBomb((Competitive) e.getGame());
    }

    @LocalEvent
    public void onBombPlant(BombPlantEvent e) {

        GameStateInstance gameState = new GameStateInstance("Bomb planted", GeneralGameStage.BOMB_PLANT);


        // replace live with bomb planted
        if (e.getGame().getGameState().getGeneralStage() == GeneralGameStage.LIVE) {
            e.getGame().setGameState(GeneralGameStage.BOMB_PLANT);
        }

        ctx.setBombCarry(null);
    }
}
