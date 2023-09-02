package obfuscate.mechanic.item.utility.grenade;

public class FireBlockData {
    public final Grenade source;
    public final Long expires;

    public FireBlockData(Grenade source, Long expires) {
        this.source = source;
        this.expires = expires;
    }

    public boolean expired() {
        return System.currentTimeMillis() >= expires;
    }
}
