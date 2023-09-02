package obfuscate.game.damage;

public class DamageModifiers {
    private boolean backstab = false;
    private boolean headshot = false;
    private Double wallbangPenalty = null;
    private boolean noscope = false;
    private boolean blind = false;

    private boolean throughSmoke = false;

    public double apply(double baseDamage) {
        if (backstab) {
            baseDamage *= 7;
        }
        if (headshot) {
            baseDamage *= 4;
        }

        if (wallbangPenalty != null) {
            // wallbangPenalty is percentage of original damage that will come through
            baseDamage *= wallbangPenalty;
        }

        return baseDamage;
    }

    public void setBackstab(boolean backstab) {
        this.backstab = backstab;
    }

    public void setHeadshot(boolean headshot) {
        this.headshot = headshot;
    }

    public void setWallbangPenalty(Double wallbangPenalty) {
        this.wallbangPenalty = wallbangPenalty;
    }

    public void setNoscope(boolean noscope) {
        this.noscope = noscope;
    }

    public void setBlind(boolean blind) {
        this.blind = blind;
    }

    public boolean isBackstab() {
        return backstab;
    }

    public boolean isHeadshot() {
        return headshot;
    }

    public double getWallbangPenalty() {
        return wallbangPenalty;
    }

    /** consider damage a wallbang if there is a penalty. penalty of 1 basically passes all damage */
    public boolean isWallbang() {
        return wallbangPenalty != null && wallbangPenalty < 1;
    }

    public boolean isNoscope() {
        return noscope;
    }

    public boolean isBlind() {
        return blind;
    }

    public void setThroughSmoke(boolean inSmoke) {
        throughSmoke = inSmoke;
    }

    public boolean isThroughSmoke() {
        return throughSmoke;
    }
}
