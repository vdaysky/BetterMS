package obfuscate.mechanic.version.hitbox;

import obfuscate.mechanic.version.PlayerLocation;
import obfuscate.util.UtilParticle;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public abstract class AbstractRectangleHitbox implements Hitbox {

    abstract double getHeadHeight();
    abstract double getHeadWidth();
    abstract double getHeadThickness();

    abstract double getBodyHeight();
    abstract double getBodyWidth();
    abstract double getBodyThickness();

    abstract double getLegsWidth();
    abstract double getLegHeight();
    abstract double getLegsThickness();


    public Location getLegCenter(PlayerLocation player)
    {
        return player.getLocation().add(new Vector(0, getLegHeight() / 2, 0)).clone();
    }

    public Location getBodyCenter(PlayerLocation player)
    {
        return player.getLocation().add(new Vector(0, getLegHeight() + getBodyHeight() / 2, 0)).clone();
    }

    public Location getHeadCenter(PlayerLocation player)
    {
        return player.getEyeLocation().clone();
    }

    public Vector getHeadDirection(PlayerLocation player)
    {
        return player.getEyeLocation().getDirection().clone();
    }

    public Vector getBodyDirection(PlayerLocation player)
    {
        return player.getEyeLocation().getDirection().setY(0).clone();
    }

    @Override
    public boolean hitHead(PlayerLocation player, Location loc)
    {
        return hitVolume(getHeadWidth(), getHeadHeight(), getHeadThickness(),
                getHeadCenter(player), getHeadDirection(player), loc);
    }

    @Override
    public boolean hitBody(PlayerLocation player, Location loc)
    {
        return hitLegs(player, loc) || hitVolume(getBodyWidth(), getBodyHeight(), getBodyThickness(),
                getBodyCenter(player), getBodyDirection(player), loc);
    }

    private boolean hitLegs(PlayerLocation player, Location loc)
    {
        return hitVolume(getLegsWidth(), getLegHeight(), getLegsThickness(),
                getLegCenter(player), getBodyDirection(player), loc);
    }

    public void showHitbox(PlayerLocation player)
    {
        showBox(player, getHeadWidth(), getHeadHeight(), getHeadThickness(), getHeadCenter(player), true, ParticleEffect.CRIT_MAGIC);
        showBox(player, getBodyWidth(), getHeadHeight(), getHeadThickness(), getBodyCenter(player), false, ParticleEffect.CRIT);
        showBox(player, getLegsWidth(), getHeadHeight(), getHeadThickness(), getLegCenter(player), false, ParticleEffect.FIREWORKS_SPARK);
    }

    private void showBox(PlayerLocation player, double width, double height, double thickness, Location base, boolean head, ParticleEffect particle)
    {
        Vector direction = getHeadDirection(player);
        if (!head)
        {
            direction = getBodyDirection(player);
        }
        Vector a = direction.clone();
        Vector b = new Vector(a.getZ(), 0, -a.getX());
        Vector c = a.clone().crossProduct(b);

        Location locX = base.clone();
        locX.add(a.clone().normalize().multiply(thickness/2));
        locX.add(b.clone().normalize().multiply(width/2));
        locX.add(c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);

        locX = base.clone();
        locX.subtract(a.clone().normalize().multiply(thickness/2));
        locX.add(     b.clone().normalize().multiply(width/2));
        locX.add(     c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);

        locX = base.clone();
        locX.add(     a.clone().normalize().multiply(thickness/2));
        locX.add(     b.clone().normalize().multiply(width/2));
        locX.subtract(c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);

        locX = base.clone();
        locX.subtract(a.clone().normalize().multiply(thickness/2));
        locX.add(     b.clone().normalize().multiply(width/2));
        locX.subtract(c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);

        locX = base.clone();
        locX.subtract(a.clone().normalize().multiply(thickness/2));
        locX.subtract(b.clone().normalize().multiply(width/2));
        locX.subtract(c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);

        locX = base.clone();
        locX.add(a.clone().normalize().multiply(thickness/2));
        locX.subtract(b.clone().normalize().multiply(width/2));
        locX.add(c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);

        locX = base.clone();
        locX.subtract(a.clone().normalize().multiply(thickness/2));
        locX.subtract(b.clone().normalize().multiply(width/2));
        locX.add(c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);

        locX = base.clone();
        locX.add(a.clone().normalize().multiply(thickness/2));
        locX.subtract(b.clone().normalize().multiply(width/2));
        locX.subtract(c.clone().normalize().multiply(height/2));
        UtilParticle.PlayParticle(particle, locX, 0, 0, 0 , 0 , 1);
    }

    private boolean hitVolume(double width, double height, double thickness,
                              Location basePoint, Vector hitboxDirection, Location loc)
    {
        Vector front = hitboxDirection.clone().normalize();
        Vector side = new Vector(front.getZ(), 0, -front.getX()).normalize();
        Vector top = front.clone().crossProduct(side).normalize();

        Vector hitVec = new Vector(loc.getX() - basePoint.getX(), loc.getY() - basePoint.getY(), loc.getZ() - basePoint.getZ());

        boolean hit = Math.abs(hitVec.clone().dot(front)) < thickness / 2 &&
                Math.abs(hitVec.clone().dot(side)) < width / 2 &&
                Math.abs(hitVec.clone().dot(top)) < height / 2;

        return hit;
    }
}
