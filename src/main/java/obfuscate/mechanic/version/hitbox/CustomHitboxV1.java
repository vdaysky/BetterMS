package obfuscate.mechanic.version.hitbox;

import obfuscate.mechanic.version.PlayerLocation;
import org.bukkit.Location;

public class CustomHitboxV1 extends AbstractRectangleHitbox
{
    @Override
    double getHeadHeight() {
        return 0.5;
    }

    @Override
    double getHeadWidth() {
        return 0.40;
    }

    @Override
    double getHeadThickness() {
        return 0.40;
    }

    @Override
    double getBodyHeight() {
        return 0.77;
    }

    @Override
    double getBodyWidth() {
        return 1.3;
    }

    @Override
    double getBodyThickness() {
        return 0.6;
    }

    @Override
    double getLegsWidth() {
        return 0.8;
    }

    @Override
    double getLegHeight() {
        return 0.8;
    }

    @Override
    double getLegsThickness() {
        return 0.75;
    }

    @Override
    public boolean hitHead(PlayerLocation player, Location loc) {
        // make outer head hits be counted as body hits
        if (!super.hitHead(player, loc)) {
            return false;
        }

        var locToCenter = getHeadCenter(player).distance(loc);
        return locToCenter < 0.3;
    }

    @Override
    public boolean hitBody(PlayerLocation player, Location loc) {
        // make outer head hits be counted as body hits
        if (super.hitHead(player, loc)) {
            var locToCenter = getHeadCenter(player).distance(loc);
            if (locToCenter >= 0.3) {
                return true;
            }
        }

        return super.hitBody(player, loc);
    }
}
