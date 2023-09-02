package obfuscate.util.time;

public class Scheduler
{
    public static Task runNextTick(Runnable lambda)
    {
        Task task = new Task(lambda, 1);
        task.run();
        return task;
    }
}
