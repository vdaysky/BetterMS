package obfuscate.util;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class UtilAction
{
    public static void velocity(Entity ent, double str, double yAdd, double yMax, boolean groundBoost)
    {
        velocity(ent, ent.getLocation().getDirection(), str, false, 0, yAdd, yMax, groundBoost);
    }

    public static void velocity(Entity ent, Vector vec, double str, boolean ySet, double yBase, double yAdd, double yMax, boolean groundBoost)
    {
        if (Double.isNaN(vec.getX()) || Double.isNaN(vec.getY()) || Double.isNaN(vec.getZ()) || vec.length() == 0)
            return;

        //YSet
        if (ySet)
            vec.setY(yBase);

        //Modify
        vec.normalize();
        vec.multiply(str);

        //YAdd
        vec.setY(vec.getY() + yAdd);

        //Limit
        if (vec.getY() > yMax)
            vec.setY(yMax);

        if (groundBoost)
            if (ent.isOnGround())
                vec.setY(vec.getY() + 0.2);

        //Velocity
        ent.setFallDistance(0);
        ent.setVelocity(vec);
    }
}
