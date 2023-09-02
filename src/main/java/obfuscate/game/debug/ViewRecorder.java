package obfuscate.game.debug;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.time.Task;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ViewRecorder {

    /**
     * Look history for every single player
     * */
    private static final HashMap<StrikePlayer, ViewHistory> histories = new HashMap<>();

    private static Long lastRecorded = 0L;

    private Task task;

    private static ViewRecorder instance;

    private ViewRecorder() {

    }

    public static ArrayList<ArrayList<Vector>> getShotHistory(String filename) {

        ArrayList<ArrayList<Vector>> shotHistory = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            ArrayList<Vector> looks = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) {
                    if (looks.size() != 0) {
                        throw new RuntimeException("Unexpected new line in the middle of block");
                    } else {
                        continue;
                    }
                }
                String[] split = line.split(" ");
                looks.add(new Vector(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2])));
                if (looks.size() == 10 + 1 + 5) {
                    shotHistory.add(looks);
                    looks = new ArrayList<>();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shotHistory;
    }

    public void start() {
        if (task == null) {
            task = new Task(this::recordViews, 1, 1).run();
            return;
        }
        MsdmPlugin.logger().warning("ViewRecorder is already running!");
    }

    public static ViewRecorder getInstance() {
        if (instance == null) {
            instance = new ViewRecorder();
        }
        return instance;
    }

    public void recordViews() {
        if (isTickRecorded()) return;
        for (StrikePlayer player : StrikePlayer.getOnline()) {

            if (player.isNPC()) {
                continue;
            }

            getHistory(player).record();
        }
    }

    public void recordFor(StrikePlayer player) {
        if (!histories.containsKey(player)) {
            histories.put(player, new ViewHistory(player));
        }
        getHistory(player).setRecording(true);
    }

    public void stopRecordingFor(StrikePlayer player) {
        getHistory(player).setRecording(false);
    }

    public ViewHistory getHistory(StrikePlayer player) {
        if (!histories.containsKey(player)) {
            histories.put(player, new ViewHistory(player));
        }
        return histories.get(player);
    }

    public void onShoot(StrikePlayer player) {

        if (getHistory(player).getViewHistory(10) == null) {
            return;
        }

        if (!getHistory(player).isRecording()) {
            return;
        }

        recordViews();

        ArrayList<double[]> elevenTicks = new ArrayList<>();

        // add last 10 ticks and current tick
        ArrayList<double[]> last11Ticks = getHistory(player).getViewHistory(11);
        elevenTicks.addAll(last11Ticks);
        Long time = System.currentTimeMillis();
        long shotAt = (long) last11Ticks.get(10)[3];
        long beforeShot = (long) last11Ticks.get(9)[3];
        System.out.println("Time: " + time + " Shot at: " + shotAt + " Before shot: " + beforeShot);

        // record 5 ticks after shot
        for (int i = 0; i < 5; i ++) {
            final int finalI = i;
            new Task(()-> {
                System.out.println("Tick " + finalI + " time: " + System.currentTimeMillis());
                var v = player.getEyeLocation().getDirection();
                elevenTicks.add(new double[]{v.getX(), v.getY(), v.getZ(), System.currentTimeMillis()});

                // if last tick was recorded, save
                if (finalI == 4) {
                    String filename = "shootHistory-" + getHistory(player).getStartedAt() + ".txt";

                    try (FileWriter fw = new FileWriter(filename, true);
                         BufferedWriter bw = new BufferedWriter(fw);
                         PrintWriter out = new PrintWriter(bw))
                    {
                        for (double[] view : elevenTicks) {
                            out.println(view[0] + " " + view[1] + " " + view[2] + " " + view[3]);
                        }
                        out.println("");
                    } catch (IOException e) {
                        e.printStackTrace();
                        MsdmPlugin.logger().severe("Error while saving view history");
                    }
                }
            }, i).run();
        }
    }

    public boolean isTickRecorded() {
        return System.currentTimeMillis() - lastRecorded < 50;
    }

}
