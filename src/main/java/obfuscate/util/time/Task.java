package obfuscate.util.time;

import obfuscate.MsdmPlugin;
import obfuscate.logging.Logger;
import org.bukkit.Bukkit;

public class Task
{
    private Integer taskID = null;
    Runnable function;
    Integer _delay;
    Integer repeat;
    private boolean isRunning = false;

    StackTraceElement[] runTrace;

    public int getID()
    {
        return taskID;
    }

    public Task(Runnable lambda, int delay_ticks)
    {
        _delay = delay_ticks;
        function = lambda;
        repeat = null;
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    public Task(Runnable lambda, int init_delay, int delay)
    {
        function = lambda;
        _delay = init_delay;
        repeat = delay;
    }

    /**
     * Wrap actual runnable and display error message,
     * as well as stack trace of .run() call to detect
     * which task is it
    */
    public Runnable wrappedFunction() {
        return () -> {
            try {
                function.run();
            } catch (Exception e) {
                Logger.critical("Task generated an exception", e);

                if (runTrace != null) {
                    Logger.info("Run Trace:");
                    for (StackTraceElement traceElement : runTrace) {
                        Logger.info(traceElement.toString());
                    }
                }
            }
        };
    }

    public Task run()
    {
        runTrace = Thread.currentThread().getStackTrace();
        isRunning = true;
        if (repeat!=null)
        {
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(MsdmPlugin.getInstance(),
                    this.wrappedFunction(), _delay, repeat);
        }
        else
        {
            taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(MsdmPlugin.getInstance(),
                    ()->{this.wrappedFunction().run(); isRunning=false;},
                    _delay);
        }
        return this;
    }

    public boolean cancel()
    {
        if (taskID == null)
            return false;

        if (isRunning)
        {
            Bukkit.getScheduler().cancelTask(taskID);
            isRunning = false;
            return true;
        }
        return false;
    }

    public void setDelay(int t_delay)
    {
        _delay = t_delay;
    }

    public void setRepeat(int t_updateRate)
    {
        repeat = t_updateRate;
    }
}
