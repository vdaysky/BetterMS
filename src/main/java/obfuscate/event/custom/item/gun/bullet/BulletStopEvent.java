package obfuscate.event.custom.item.gun.bullet;

import obfuscate.event.custom.CustomEvent;
import obfuscate.game.core.Game;
import obfuscate.mechanic.item.guns.Bullet;

public class BulletStopEvent extends CustomEvent {

    private Bullet bullet;

    public BulletStopEvent(Bullet bullet) {
        this.bullet = bullet;
    }

    public Bullet getBullet() {
        return bullet;
    }

    public Game getGame() {
        return bullet.getShooter().getGame();
    }
}
