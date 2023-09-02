package obfuscate.game.config;

public class ConfigValue
{
    public static ConfigValue INFINITY = new ConfigValue(-1);
    public static ConfigValue TRUE = new ConfigValue(1);
    public static ConfigValue FALSE = new ConfigValue(0);

    private final Integer value;

    public ConfigValue(Integer i)
    {
        value = i;
    }

    public boolean bool()
    {
        return TRUE.eq(value);
    }

    public Integer val()
    {
        return value;
    }

    public boolean eq(int i)
    {
        return value == i;
    }

    public boolean eq(ConfigValue i)
    {
        return value.equals(i.val());
    }

    public boolean infinite()
    {
        return INFINITY.eq(value);
    }

    public int ticks()
    {
        return val() * 20;
    }
}
