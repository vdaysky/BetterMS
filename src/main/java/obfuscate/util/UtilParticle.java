package obfuscate.util;

import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.Location;

public class UtilParticle
{
    public static void PlayParticle(ParticleEffect particleName, Location loc, float offsetX, float offsetY, float offsetZ, int speed, int amount)
    {
        particleName.display(offsetX, offsetY, offsetZ, speed, amount, loc, 1000);
    }

    public static void PlayCrit(Location loc) {
        PlayParticle(ParticleEffect.CRIT, loc, 0.0f, 0.0f, 0.0f, 0, 1);
    }

    public static void PlaySpark(Location loc) {
        PlayParticle(ParticleEffect.FIREWORKS_SPARK, loc, 0.0f, 0.0f, 0.0f, 0, 1);
    }
}
