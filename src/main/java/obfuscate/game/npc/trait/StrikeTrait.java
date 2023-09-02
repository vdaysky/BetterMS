package obfuscate.game.npc.trait;

import net.citizensnpcs.api.trait.Trait;

/**
 * Base trait for all bots.
 * Contains methods of typical player lifecycle
 */
public abstract class StrikeTrait extends Trait {

    protected StrikeTrait(String name) {
        super(name);
    }

    public void onDeath() {

    }

    public void onRespawn() {

    }
}
