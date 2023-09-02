package obfuscate.mechanic.item.objective;


import obfuscate.event.custom.objective.*;
import obfuscate.game.core.Game;
import obfuscate.game.core.GameInventory;
import obfuscate.game.core.GameSession;
import obfuscate.game.config.ConfigField;
import obfuscate.game.damage.DamageReason;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.message.MsgSender;
import obfuscate.message.MsgType;
import obfuscate.team.StrikeTeam;
import obfuscate.util.UtilParticle;
import obfuscate.util.chat.C;
import obfuscate.util.time.Task;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Bomb extends StrikeItem
{
    private int bombTimeTicks= -1;
    private int defuseTime = -1;
    private int plantProgress = 0;

    private Block block;

    private Task bombTask = null;
    private Task bombPlant = null;
    private Task defuseTask = null;

    private boolean beingPlanted = false;
    private boolean isPlanted = false;
    private boolean exploded = false;
    private boolean isBeingDefused = false;

    private StrikePlayer defuser = null;

    private StrikePlayer planter = null;
    private boolean defused = false;

    private long lastBombTick = 0;

    public Bomb()
    {
        super(MiscItemType.BOMB);
    }

    @Override
    public boolean droppable() {
        return true;
    }

    public boolean canPlant(Game game, StrikePlayer player)
    {
        return game.getGameMap().isSite(player.getLocation()) && player.getPlayer().isOnGround();
    }

    @Override
    public boolean canPickup(Game game, StrikePlayer player) {

        var session = game.getGameSession(player);

        if (!session.isAlive())
            return false;

        var roster = game.getPlayerRoster(player);

        if (roster == null) {
            return false;
        }

        return roster.getTeam() == StrikeTeam.T;
    }

    public void startPlant(Game game, StrikePlayer player)
    {
        if (isBeingPlanted())
            return;

        beingPlanted = true;

        new BombPlantStartEvent(player, game, this).trigger();
        game.getSoundManager().bombPlant().forTeam(StrikeTeam.T).play();

        GameSession session = game.getGameSession(player);
        session.setPlanting(true);

        // make sound
        Runnable playSound = ()-> game.getSoundManager().bombTick().by(player).soundDistance(32).play();

//        player.giveShield();

        // beep-beep
        playSound.run();
        new Task(playSound, 5).run();

        // start 10 ticks later because it takes time to start blocking
        bombPlant = new Task(()->bombPlantTick(game, player), 10, 1);
        bombPlant.run();
    }

    public void cancelPlant(Game game, StrikePlayer player)
    {
        if (!isBeingPlanted())return;
        beingPlanted = false;
        new BombPlantStopEvent(player, game, this).trigger();

        GameSession session = game.getGameSession(player);
        bombPlant.cancel();
//        player.removeShield();

        session.setPlanting(false);
        plantProgress = 0;
    }

    private void plant(Game game, StrikePlayer player)
    {
        planter = player;
        beingPlanted = false;
        isPlanted = true;

        new BombPlantEvent(player, game, this).trigger();

        game.getSoundManager().bombPlanted().play();
        game.getSoundManager().harp().play();

        for (StrikePlayer p : game.getOnlinePlayers()) {
            p.sendTitle(" ", C.cRed + C.Bold + "Bomb has been planted!", 0, 60, 10);
        }

        game.broadcast(
                MsgSender.GAME,
                C.cRed + C.Bold + player.getName() + " has planted the bomb!",
                MsgType.CHAT
        );

        bombTimeTicks = game.getConfig().getDuration(GeneralGameStage.BOMB_PLANT) * 20;

        // place bomb
        block = player.getLocation().getBlock();
//        if (UtilBlock.solid(block))
//        {
//            block = block.getRelative(0, 1, 0);
//        }
        if (game.getRestore().GetBlocks().containsKey(block)) {
            game.getRestore().Restore(block);
        }

        game.getRestore().MakeRevertibleChange(
            block,
            Material.DAYLIGHT_DETECTOR,
            99999999999999999L
        );

        // remove bomb from inventory
        GameInventory inventory = game.getGameSession(player).getInventory();
        inventory.remove(getSlot());

        // start bomb task
        bombTask = new Task(()->bombTick(game), 0, 1);
        bombTask.run();

    }

    @Override
    public StrikeItem copy() {
        return null;
    }

    private void bombPlantTick(Game game, StrikePlayer player)
    {
        //todo move to cfg
        final int plantTime = 70; // 3.5 seconds
        final int progressbarLength = 40;

        if (player.sinceLastRightClick() > 250)
        {
//            player.sendMessage(MsgSender.GAME, C.cGray + "you stopped planting");
            cancelPlant(game, player);
            return;
        }

        if (plantProgress >= plantTime)
        {
            cancelPlant(game, player);
            plant(game, player);
            return;
        }

        float percent = plantProgress / (float)plantTime;
        plantProgress++;

        int doneLen = (int) (progressbarLength * percent);
        int toDoLen = progressbarLength - doneLen;
        String done = new String(new char[doneLen]).replace("\0", "|");
        String todo = new String(new char[toDoLen]).replace("\0", "|");
        String progress = ChatColor.GREEN + C.Bold + done + ChatColor.GRAY + C.Bold + todo;

        player.sendTitle(C.cRed + C.Bold + "Planting Bomb", progress, 0, 5, 0);
    }


    public void explode(Game game)
    {
        new BombExplodeEvent(game, this).trigger();

        Location loc = block.getLocation().add(new Vector(0.5, 0.5, 0.5));
        game.getSoundManager().bombExplode().play();
        UtilParticle.PlayParticle(ParticleEffect.EXPLOSION_HUGE, loc, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0, 5);

        // todo: balance bomb damage
        game.getDamageManager().handleExplosion(
                game, null, block.getLocation(),
                game.getConfig().getValue(ConfigField.BOMB_DAMAGE).val(),
                game.getConfig().getValue(ConfigField.MAX_BOMB_DAMAGE_DIST).val(),
                0.98,
                this, DamageReason.BOMB
        );

        remove(false);
        exploded = true;
    }

    public void remove(boolean defused)
    {
        if (block != null && !defused) {
            block.setType(Material.AIR);
            block = null;
        }

        if (bombTask != null) {
            bombTask.cancel();
            bombTask = null;
        }

        if (bombPlant != null) {
            bombPlant.cancel();
            bombPlant = null;
        }
    }

    private void bombTick(Game game)
    {
        if (bombTimeTicks == 0)
        {
            bombTask.cancel();
            explode(game);
            return;
        }

        bombTimeTicks --;

        // effects
        Location loc = block.getLocation().add(new Vector(0.5, 0.5, 0.5));

        float max_bomb_time_ticks = (float) game.getConfig().getDuration(GeneralGameStage.BOMB_PLANT) * 20;
        float max_tick_delay = game.getConfig().getValue(ConfigField.BOMB_TICK_TIME).val();
        float min_tick_delay = 3;
        double delay_ticks = Math.atan((max_tick_delay-min_tick_delay)/max_bomb_time_ticks) * bombTimeTicks + min_tick_delay;
        double delay_ms = delay_ticks * 50;

        if (System.currentTimeMillis() - lastBombTick < delay_ms)
            return;

        lastBombTick = System.currentTimeMillis();
        UtilParticle.PlayParticle(ParticleEffect.FIREWORKS_SPARK, loc, 0, 0, 0, 0, 1);
        game.getSoundManager().bombTick().at(loc).soundDistance(32).play();
    }

    public void startDefuse(Game game, StrikePlayer player)
    {
        if (!game.getGameSession(player).isAlive()) {
            return;
        }

        if (isBeingDefused){
            return;
        }

        new BombDefuseStartEvent(player, game, this).trigger();

        // defuse sound
        game.getSoundManager().bombDefuse().at(block.getLocation()).soundDistance(32).play();

        isBeingDefused = true;

        player.sendMessage(MsgSender.GAME, C.cGray + "You are defusing");
        defuseTime = game.getConfig().getValue(ConfigField.BASE_DEFUSE_TIME).ticks();

        if (game.getGameSession(player).getInventory().hasKit())
            defuseTime /= 2;

        defuseTask = new Task(()->defuseTick(game, player),0,1);
        defuseTask.run();
    }

    private void defuse(Game game, StrikePlayer player)
    {
        defuser = player;
        defused = true;

        new BombDefuseEvent(player, game, this).trigger();
        game.getSoundManager().bombDefused().play();
        remove(true);
    }

    private void defuseTick(Game game, StrikePlayer player)
    {
        int progressbarLength = 40;

        if (exploded) {
            stopDefuse(game, player);
            return;
        }

        if (!player.isAlive(game)) {
            stopDefuse(game, player);
            return;
        }

        if (defuseTime == 0)
        {
            stopDefuse(game, player);
            defuse(game, player);
        }

        if (!canSee(player)) {
            stopDefuse(game, player);
        }

        int defuse = game.getConfig().getValue(ConfigField.BASE_DEFUSE_TIME).ticks();

        if (game.getGameSession(player).getInventory().hasKit())
            defuse /= 2;

        float percent = ( defuseTime / (float) defuse );

        int doneLen = (int) (progressbarLength * percent);
        int toDoLen = progressbarLength - doneLen;

        //Bukkit.broadcastMessage("percent: " + percent + " done: " + doneLen + " to do " + toDoLen);


        String done = new String(new char[doneLen]).replace("\0", "|");
        String todo = new String(new char[toDoLen]).replace("\0", "|");
        String progress = ChatColor.GREEN + C.Bold + done + C.cGray + C.Bold + todo;

        player.sendTitle(C.cAqua + C.Bold + "Defusing Bomb", progress, 0, 5, 0);

        defuseTime--;

    }

    public boolean canSee(StrikePlayer player)
    {
        assert player.isOnline();

        Set<Material> seeThrough = new HashSet<>();
        seeThrough.add(Material.AIR);
        seeThrough.add(Material.NETHER_PORTAL);
        Block targetBlock = player.getPlayer().getTargetBlock(seeThrough, 3);
        return block.getLocation().distance(targetBlock.getLocation()) == 0;
    }

    public void stopDefuse(Game Game, StrikePlayer player)
    {
        if (!isBeingDefused)return;
        new BombDefuseStopEvent(player, Game, this).trigger();
        defuseTime = -1;
        defuseTask.cancel();
        isBeingDefused = false;
    }

    public boolean isBeingPlanted()
    {
        return beingPlanted;
    }

    public boolean isExploded()
    {
        return exploded;
    }

    public boolean isPlanted()
    {
        return isPlanted;
    }

    public boolean isDefused()
    {
        return defused;
    }

    public boolean isActive() {
        return isPlanted() && !isDefused();
    }

    public StrikePlayer getDefuser() {
        return defuser;
    }

    public StrikePlayer getPlanter() {
        return planter;
    }

    public Block getBlock() {
        return block;
    }
}
