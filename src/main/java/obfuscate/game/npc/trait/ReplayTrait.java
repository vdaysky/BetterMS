package obfuscate.game.npc.trait;

import obfuscate.game.core.Game;
import obfuscate.game.npc.PrerecordedPath;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.time.Task;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class ReplayTrait extends StrikeTrait
{
    private final PrerecordedPath path;
    private final Game Game;
    private Task runTask = null;
    private boolean invisible = false;

    public ReplayTrait(Game Game, PrerecordedPath path)
    {
        super("ReplayTrait");
        this.path = path;
        this.Game = Game;
    }

    public boolean isRunning()
    {
        return runTask != null;
    }

    public void stopRun()
    {
        makeInvisible();
        getNPC().setProtected(true);
        getPlayer().setInvulnerable(this.Game, true);

        runTask.cancel();
        runTask = null;
    }

    public void startRunning()
    {
        getNPC().setProtected(false);
        getPlayer().setInvulnerable(this.Game, false);
        new Task(this::makeVisible, 10).run();

        AtomicInteger i = new AtomicInteger();
        Task task = new Task(()->{
            teleportToPoint(path.getPoints().get(i.get()));
            i.getAndIncrement();
            if (path.getPoints().size() == i.get())
            {
                onReachEnd();
            }
        }, 1, 1);
        task.run();
        runTask = task;
    }

    public void makeInvisible()
    {
        if (invisible)return;
        invisible = true;

        for (Player p : npc.getEntity().getWorld().getPlayers())
            p.hidePlayer((Player) getNPC().getEntity());
    }

    public void makeVisible()
    {
        if (!invisible)return;
        invisible = false;
        for (Player p : npc.getEntity().getWorld().getPlayers())
            p.showPlayer((Player) getNPC().getEntity());
    }

    public void onGameJoin()
    {
        if (!isRunning())
            startRunning();
    }

    public StrikePlayer getPlayer() {
        return StrikePlayer.getOrCreate(getNPC());
    }

    @Override
    public void onDeath() {
        stopRun();
    }

    @Override
    public void onRespawn() {
        startRunning();
    }

    private void onReachEnd()
    {
        stopRun();
        int randomRespawnDelay = (int)(100 * Math.random());
        new Task(this::startRunning, randomRespawnDelay).run();
    }

    private void teleportToPoint(Double[] loc)
    {
        getNPC().teleport(new Location(getNPC().getEntity().getWorld(), loc[0], loc[1], loc[2], (float)(double)loc[3], (float)(double)loc[4]), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public void onRemove()
    {
        getPlayer().getGame().tryLeavePlayer(getPlayer());
    }
}
