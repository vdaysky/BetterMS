package obfuscate.game.npc;

import obfuscate.mechanic.version.hitbox.CustomHitboxV1;
import obfuscate.mechanic.version.PlayerLocation;
import obfuscate.util.time.Task;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HitboxTest extends Trait {
    public HitboxTest() {
        super("hitboxTest");
    }

    @Override
    public void onSpawn()
    {
        CustomHitboxV1 hitbox = new CustomHitboxV1();
        Player p = ((Player) getNPC().getEntity());
        Location loc = p.getLocation();
        Location eyeLoc = p.getEyeLocation();
                
        new Task(()->hitbox.showHitbox(new PlayerLocation(loc, eyeLoc)), 1, 5).run();
    }

}
