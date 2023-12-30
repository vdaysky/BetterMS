package obfuscate.game.debug;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.Promise;
import obfuscate.util.time.Task;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class ViewPlayer {

    public static Promise<?> playSingleView(String viewName, StrikePlayer player, ArrayList<Vector> vs) {
        Promise<?> promise = new Promise<>();
        player.sendMessage("Start of view " + viewName);
        int i = 0;
        for (Vector v : vs) {
            final int finalI = i;
            new Task(() -> {
                if (finalI < 10) {
                    player.sendMessage("Tick " + finalI + " | before shot");
                } else if (finalI == 10) {
                    player.sendMessage("Tick " + finalI + " tick of shot");
                } else {
                    player.sendMessage("Tick " + finalI + " | " + (finalI - 10) + " ticks after shot");
                }
                Player p = player.getPlayer();
                // set player look direction
                p.teleport(p.getLocation().setDirection(v));
                if (finalI + 1 == vs.size()) {
                    player.sendMessage("End of view");
                    promise.fulfill(null);
                }
            }, i * 20).run();
            i += 1;


        }
        return promise;
    }
    // shootHistory-1673084239058.txt
    public static Promise<?> playAllViews(StrikePlayer player, ArrayList<ArrayList<Vector>> shots) {

        Promise<?> promise = null;

        int vIdx = 0;
        for (var shot : shots) {
            final int fVIdx = vIdx;

            if (promise == null) {
                promise = playSingleView("#" + fVIdx, player, shot);
            } else {
                promise = promise.thenAsync((x) -> playSingleView("#" + fVIdx, player, shot));
            }

            vIdx += 1;
        }

        return promise;
    }
}
