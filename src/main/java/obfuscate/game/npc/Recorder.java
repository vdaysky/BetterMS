package obfuscate.game.npc;

import obfuscate.game.player.StrikePlayer;
import obfuscate.util.time.Task;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Recorder
{
    private StrikePlayer player;
    private String pathName;
    private ArrayList<Double[]> points = new ArrayList<>();
    private Task recordingTask = null;
    private static HashMap<StrikePlayer, Recorder> activeRecorders = new HashMap<>();
    private boolean shutdown = false;
    public Recorder(StrikePlayer actor, String recordName)
    {
        player = actor;
        pathName = recordName;
        activeRecorders.put(player, this);
    }

    public static Recorder getActive(StrikePlayer admin)
    {
        return activeRecorders.get(admin);
    }

    public boolean wasForciblyShutDown()
    {
        return shutdown;
    }

    public boolean start()
    {
        int TICK_RECORDING_LIMIT = 20000; // 100 seconds

        if (recordingTask != null)return false;
        AtomicInteger c = new AtomicInteger();

        recordingTask = new Task(()->
        {
            Location loc = player.getLocation();
            points.add(new Double[]{loc.getX(), loc.getY(), loc.getZ(), (double) loc.getYaw(), (double) loc.getPitch()});
            c.getAndIncrement();
            if (c.get() >= TICK_RECORDING_LIMIT){
                shutdown = true;
                recordingTask.cancel();
            }
        }, 1, 1);
        recordingTask.run();
        return true;
    }

    public boolean stop()
    {
        if (recordingTask == null) return false;

        activeRecorders.remove(this);
        recordingTask.cancel();
        recordingTask = null;
        return true;
    }

    public boolean saveRecording()
    {
        if (player.getGame() == null)
            return false;

        PrerecordedPath.save(player.getGame().getGameMap(), pathName, points);
        return true;
    }
}
