package obfuscate.event.custom.item.gun.bullet;

import obfuscate.event.custom.CancellableEvent;
import obfuscate.game.core.Game;
import obfuscate.mechanic.item.guns.Bullet;
import org.bukkit.Location;


public class BulletWallbangEvent extends CancellableEvent {

    private Bullet bullet;
    private Location rightAfterHit;

    private Location preHit;
    private Location afterHit;

    private Location preComeOutLoc;
    private Game game;

    public BulletWallbangEvent(Game game, Bullet bullet, Location rightAfterHit, Location preHit, Location comeOutLoc, Location preComeOutLoc) {
        this.bullet = bullet;
        this.preHit = preHit; // right before come in
        this.rightAfterHit = rightAfterHit.clone(); // as soon as possible after hit happened
        this.afterHit = comeOutLoc.clone(); // right after came out
        this.preComeOutLoc = preComeOutLoc; // right before come out
        this.game = game;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Location getPreHitLocation() {
        return preHit.clone();
    }

    public Location getHitLocation() {
        return rightAfterHit.clone();
    }

    public Location getPreComeOutLocation() {
        return preComeOutLoc.clone();
    }

    public Location getComeOutLocation() {
        return afterHit.clone();
    }

    public double getTravelledDistance() {
        return rightAfterHit.distance(afterHit);
    }

    public Game getGame() {
        return game;
    }
}
