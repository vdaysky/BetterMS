package obfuscate.game.damage;

public class DamageSourceWrapper implements NamedDamageSource {

    private String damageSourceName;

    public static final DamageSourceWrapper INTENDED_DESIGN = new DamageSourceWrapper("intended game design");

    public DamageSourceWrapper(String damageSourceName) {
        this.damageSourceName = damageSourceName;
    }

    @Override
    public String getName() {
        return damageSourceName;
    }
}
