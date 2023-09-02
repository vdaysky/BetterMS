package obfuscate.game.debug;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.guns.Bullet;

import java.util.ArrayList;
import java.util.HashMap;

public class HitRegLog {

    private final HashMap<Bullet, BulletLog> bulletLogs = new HashMap<>();
    private final HashMap<StrikePlayer, ArrayList<BulletLog>> playerLogs = new HashMap<>();

    public void addBulletLog(BulletLog bulletLog) {
        StrikePlayer shooter = bulletLog.getBullet().getShooter();

        bulletLogs.put(bulletLog.getBullet(), bulletLog);
        var logs = playerLogs.getOrDefault(shooter, new ArrayList<>());
        playerLogs.put(bulletLog.getBullet().getShooter(), logs);

        logs.add(bulletLog);
    }
    public BulletLog getBulletLog(Bullet bullet) {
        return bulletLogs.get(bullet);
    }

    public BulletLog getPlayerShot(StrikePlayer playerToDebug, Integer shotToDebug) {
        var logs = playerLogs.get(playerToDebug);
        if (logs == null) {
            MsdmPlugin.warn("Logs array for player " + playerToDebug.getName() + " is not initialized");
            return null;
        }
        int size = logs.size();
        int idx = size + shotToDebug;

        if (idx >= logs.size() || idx < 0) {
            MsdmPlugin.logger().warning("Log index " + shotToDebug + " is out of bounds: there are " + size + " logs");
        }

        return logs.get(idx);
    }
}
