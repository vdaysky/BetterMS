package obfuscate.game.damage;

public class DamageState
{
    double _rawDamage;
    int _multiplier;
    DamageReason reason;
    String description;

    public DamageState(double rawDamage, int multiplier, DamageReason reason, String description)
    {
        _rawDamage = rawDamage;
        _multiplier = multiplier;
        this.reason = reason;
        this.description = description;
    }

    public DamageState(double rawDamage, int multiplier, DamageReason reason)
    {
        _rawDamage = rawDamage;
        _multiplier = multiplier;
        this.reason = reason;
        this.description = reason.toString();
    }

    public double getDamage()
    {
        return _rawDamage * _multiplier;
    }
    public boolean wasHeadshot()
    {
        return _multiplier == 2;
    }

    public int getMultiplier()
    {
        return _multiplier;
    }
    public DamageReason getReason()
    {
        return reason;
    }
    public String getDescription()
    {
        return description;
    }


}
