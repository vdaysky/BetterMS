package obfuscate.game.damage;

public enum DamageReason
{
    FALL("Fall"),
    FIRE("Fire"),
    KNIFE("Knife"),
    GUN("Gun"),
    BOMB("Bomb explosion"),
    GRENADE("Grenade"),
    CANCELLED("Cancelled"),

    NONE("None")

    ;

    String reason;
    DamageReason(String reason)
    {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return reason;
    }
}
