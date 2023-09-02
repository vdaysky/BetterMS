package obfuscate.game.debug;

import obfuscate.game.player.StrikePlayer;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class ViewHistory {

    private long lastRecorded = 0L;
    private ArrayList<double[]> viewHistory = new ArrayList<>();

    private StrikePlayer player;

    private boolean recording = false;

    private Long startedAt = 0L;


    public ViewHistory(StrikePlayer player) {
        this.player = player;
    }

    public void record() {
        if (isTickRecorded()) {
            return;
        }
        Vector look = player.getPlayer().getEyeLocation().getDirection();
        viewHistory.add(new double[]{look.getX(), look.getY(), look.getZ(), System.currentTimeMillis()});
        if (viewHistory.size() > 20) {
            viewHistory.remove(0);
        }
        lastRecorded = System.currentTimeMillis();
    }

    public void reset() {
        viewHistory.clear();
    }

    public ArrayList<double[]> getViewHistory(int ticks) {
        if (ticks > viewHistory.size()) {
            return null;
        }
        ArrayList<double[]> history = new ArrayList<>();
        // take slice of viewHistory
        for (int i = viewHistory.size() - ticks; i < viewHistory.size(); i++) {
            history.add(viewHistory.get(i));
        }
        return history;
    }

    public boolean isTickRecorded() {
        return System.currentTimeMillis() - lastRecorded < 50;
    }

    public void setRecording(boolean isRecording) {
        this.recording = isRecording;
        if (isRecording) {
            startedAt = System.currentTimeMillis();
        }
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public boolean isRecording() {
        return recording;
    }
}
