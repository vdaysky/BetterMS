package obfuscate.util.recahrge;

import java.util.HashMap;

public class ARecharge<Type> implements IRecharge<Type>
{
    private final HashMap<Type, HashMap<String, Long>> cooldowns = new HashMap<>();

    @Override
    public boolean done(Type anchor, String ability, Long delay)
    {
        cooldowns.computeIfAbsent(anchor, k -> new HashMap<>());
        if (cooldowns.get(anchor).get(ability) == null || cooldowns.get(anchor).get(ability) + delay <= System.currentTimeMillis() )
        {
            cooldowns.get(anchor).put(ability, System.currentTimeMillis());
            return true;
        }
        return false;
    }
}
