package obfuscate.mechanic.version.hitbox;

public class SniperReducedHitbox extends AbstractRectangleHitbox {
    @Override
    double getHeadHeight() {
        return 0.40;
    }

    @Override
    double getHeadWidth() {
        return 0.40;
    }

    @Override
    double getHeadThickness() {
        return 0.45;
    }

    @Override
    double getBodyHeight() {
        return 0.70;
    }

    @Override
    double getBodyWidth() {
        return 1.15;
    }

    @Override
    double getBodyThickness() {
        return 0.50;
    }

    @Override
    double getLegsWidth() {
        return 0.60;
    }

    @Override
    double getLegHeight() {
        return 0.80;
    }

    @Override
    double getLegsThickness() {
        return 0.60;
    }
}
