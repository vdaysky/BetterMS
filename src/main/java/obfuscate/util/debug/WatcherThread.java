package obfuscate.util.debug;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WatcherThread extends Thread {

    Thread main;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public WatcherThread(Thread main) {
        this.main = main;
    }

    private void printThreadState() {
        for (var line : main.getStackTrace()) {
            System.out.println("[Watcher] " + line);
        }
    }

    @Override
    public void run() {
        scheduler.scheduleAtFixedRate(this::printThreadState, 0, 10, TimeUnit.SECONDS);
    }
}
