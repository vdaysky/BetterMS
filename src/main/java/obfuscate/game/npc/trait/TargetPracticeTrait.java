package obfuscate.game.npc.trait;

import obfuscate.mechanic.version.hitbox.CustomHitboxV1;
import obfuscate.mechanic.version.PlayerLocation;
import obfuscate.util.time.Task;
//import net.citizensnpcs.nms.v1_17_R1.entity.EntityHumanNPC;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class TargetPracticeTrait extends StrikeTrait {

    private boolean isMoving = false;
    private boolean showHitbox = false;
    private Task showHitboxTask;

    private Location currentTarget;

    public TargetPracticeTrait(String name) {
        super(name);
    }

    public void setMoving(boolean moving) {
        if (moving == isMoving) return;
        isMoving = moving;
    }

    private boolean canNav(Location location) {
        Location start = npc.getEntity().getLocation();
        Vector direction = location.toVector().subtract(start.toVector()).normalize();

        while(start.distance(location) > 1) {
            start.add(direction);
            Block block = start.getBlock();
            if (block.getType().isSolid()) return false;
        }
        return true;
    }

    public void setShowHitbox(boolean value) {
        this.showHitbox = value;
        if (!value && showHitboxTask != null) {
            showHitboxTask.cancel();
            showHitboxTask = null;
        }
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) return;

        if (showHitbox && showHitboxTask == null) {
            CustomHitboxV1 hitbox = new CustomHitboxV1();
            Player p = ((Player) getNPC().getEntity());
            Location loc = p.getLocation();
            Location eyeLoc = p.getEyeLocation();

            showHitboxTask = new Task(()->hitbox.showHitbox(new PlayerLocation(loc, eyeLoc)), 1, 5).run();
        }

        if (!isMoving) {
            if (npc.getNavigator().isNavigating()) {
                currentTarget = null;
                npc.getNavigator().cancelNavigation();
            }
            return;
        }

        // jump
//        var ent = (EntityHumanNPC.PlayerNPC) getNPC().getEntity();
//        ent.getHandle().getControllerJump().jump();

        do {
            currentTarget = npc.getEntity().getLocation().add(new Vector(new Random().nextDouble() * 14 - 7, 0, 0));
        } while (!canNav(currentTarget));

        ArrayList<Vector> actions = new ArrayList<>();
        actions.add(currentTarget.toVector());
        npc.getNavigator().setTarget(actions);
    }
}
