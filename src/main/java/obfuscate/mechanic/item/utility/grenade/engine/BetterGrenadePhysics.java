package obfuscate.mechanic.item.utility.grenade.engine;

import obfuscate.MsdmPlugin;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.util.alg.UtilAlg;
import obfuscate.util.block.UtilBlock;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.item.EntityItem;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BetterGrenadePhysics implements GrenadePhysics {

    private Vector currentVelocity;
    private Location currentLocation;

    private EntityItem _entity;

    private boolean _isOnGround = false;

    private boolean _touchedGround = false;

    protected int groundTicks = 0;


    @Override
    public Location getCurrentLocation() {
        return currentLocation.clone();
    }

    @Override
    public void launch(ItemStack stack, StrikePlayer player, boolean wasLeftClick, Game game) {
        currentLocation = player.getEyeLocation().add(player.getLocation().getDirection());

        _entity = new EntityItem(
                ((CraftWorld) player.getWorld()).getHandle(),
                currentLocation.getX(),
                currentLocation.getY(),
                currentLocation.getZ(),
                CraftItemStack.asNMSCopy(stack)
        );
        _entity.setNoGravity(true);
        _entity.setItemStack(CraftItemStack.asNMSCopy(stack));
        _entity.setPosition(currentLocation.getX(), currentLocation.getY(), currentLocation.getZ());
        _entity.setInvulnerable(true);
        _entity.setMot(0, 0, 0);

        // play spawn packet
        var pack = new PacketPlayOutSpawnEntity(_entity);

        player.getPlayer().getWorld().getPlayers().forEach(p -> ((CraftPlayer)p).getHandle().b.sendPacket(pack));

        PacketPlayOutEntityMetadata meta = new PacketPlayOutEntityMetadata(_entity.getId(), _entity.getDataWatcher(), true);
        player.getPlayer().getWorld().getPlayers().forEach(p -> ((CraftPlayer)p).getHandle().b.sendPacket(meta));

        Vector directThrow;

        float speed;
        if (wasLeftClick) {
            speed = game.getConfig().getValue(ConfigField.GRENADE_SHORT_THROW_SPEED).val() / 100f; // 0.2
        } else {
            speed = game.getConfig().getValue(ConfigField.GRENADE_DIRECT_THROW_SPEED).val() / 100f; // 1.7
        }
        Vector throwLook = player.getEyeLocation().getDirection();

        // lift a bit
        throwLook.add(
                new Vector(
                        0,
                        game.getConfig().getValue(ConfigField.GRENADE_ARM_SWING_FACTOR).val() / 100f,
                        0
                )
        );

        directThrow = throwLook.multiply(speed);

        // player is moving
        if (game.getConfig().getValue(ConfigField.MOVEMENT_AFFECT_GRENADE).bool() && player.sinceMove() < 90) {
            Vector movement = player.getAverageMove(500);

            // see how player moving relative to look direction
            Vector look = player.getEyeLocation().getDirection();
            UtilAlg.VectorPair pair = UtilAlg.relativeComponents(movement, look);

            Vector forward = pair.getCollinear();

            // scale strafe and forward
            double forwardFactor = 0.5;
            forward.multiply(forwardFactor);

            // cap strafe and forward
            double forwardLimit = 1.6D;

            if (forward.length() > forwardLimit) {
                forward.normalize().multiply(forwardLimit);
            }

            // add strafe and forward components to throw
            directThrow.add(forward);
        }

        currentVelocity = directThrow;
    }

    @Override
    public void removeEntity() {
        if (_entity != null) {
            var pack = new PacketPlayOutEntityDestroy(_entity.getId());
            currentLocation.getWorld().getPlayers().forEach(p -> ((CraftPlayer)p).getHandle().b.sendPacket(pack));
        }
    }

    /** Computes grenade acceleration */
    private Vector getNextTickVelocity(Vector vel, Game game)
    {
        int g_mod = game.getConfig().getValue(ConfigField.G_MOD).val(); // 5 by def.
        double g = 9.81 / (20 * g_mod); // 0.5 blocks per tick 9.8 blocks per second

        double xVel = vel.getX();
        double yVel = vel.getY();
        double zVel = vel.getZ();
        // actually free fall is 2.7 blocks/tick, but I don't care
        return new Vector(xVel, Math.max(-1, yVel - g), zVel);
    }

    @Override
    public boolean update(Game game) {
        // if one tenth below the block is solid, consider on ground
        _isOnGround = UtilBlock.isInsideBlock((currentLocation.add(new Vector(0, -0.1, 0))));

        if (_isOnGround) {
            _touchedGround = true;
            groundTicks += 1;
        }
        else {
            groundTicks = 0;
        }

        // update velocity (affected by gravity each tick)
        currentVelocity = getNextTickVelocity(currentVelocity, game);

        // Air resistance
        if (currentVelocity.length() != 0) {
            float airRes = game.get(ConfigField.GRENADE_AIR_RES) / 100f;
            currentVelocity.multiply(new Vector(airRes, airRes, airRes));
        }
        return false;
    }

    @Override
    public boolean touchedGround() {
        return _touchedGround;
    }

    @Override
    public void multiplyVelocity(float f) {
        currentVelocity = currentVelocity.multiply(f);
    }

    private boolean didNotBreakGlass(Game game, Location loc) {
        if (UtilBlock.isGlassPane(loc.getBlock())) {
            game.breakGlass(loc.getBlock());
            currentVelocity = currentVelocity.multiply(0.8);
            return false;
        }
        return true;
    }

    @Override
    public int getGroundTicks() {
        return groundTicks;
    }

    @Override
    public void moveRebounding(Game game) {
        final float REBOUND_HOR_SPEED_LOSS = game.get(ConfigField.GRENADE_REBOUND_HOR_PRESERVE) / 100f;
        final float REBOUND_VERT_SPEED_LOSS = game.get(ConfigField.GRENADE_REBOUND_VERT_PRESERVE) / 100f;

        Location intermediateLoc = currentLocation.clone();
        double tinyMoveLength = 0.01;
        double moveCount = currentVelocity.length() / tinyMoveLength;
        Vector tinyMove = currentVelocity.clone().normalize().multiply(tinyMoveLength);

        for (int i = 0; i < moveCount; i++)
        {
            Location xMove = intermediateLoc.clone().add(tinyMove.getX(), 0, 0);
            Location yMove = intermediateLoc.clone().add(0, tinyMove.getY(), 0);
            Location zMove = intermediateLoc.clone().add(0, 0, tinyMove.getZ());

            if (UtilBlock.isInsideBlock(intermediateLoc)) {
                MsdmPlugin.severe("Grenade is inside block! (1)");
            }

            // we are inside block, meaning we need to rebound
            if (UtilBlock.isInsideBlock(xMove)) {
                if (Math.abs(currentVelocity.getX()) > 0.01) {
                    if (didNotBreakGlass(game, xMove)) {
                        // sound
                        game.getSoundManager().grenadeRebound().at(getCurrentLocation()).pitch(2f).play();

                        // rebound
                        currentVelocity.setX(-currentVelocity.getX() * REBOUND_HOR_SPEED_LOSS);

                        // recalculating tiny move with new X velocity
                        tinyMove = currentVelocity.clone().normalize().multiply(tinyMoveLength);
                    }
                } else {
                    currentVelocity.setX(0);
                }
            } else {
                // do tiny move in X direction
                intermediateLoc.add(tinyMove.getX(), 0, 0);
            }

            if (UtilBlock.isInsideBlock(intermediateLoc)) {
                MsdmPlugin.severe("Grenade is inside block! (2)");
            }

            // we are inside block, meaning we need to rebound
            if (UtilBlock.isInsideBlock(zMove)) {

                if (Math.abs(currentVelocity.getZ()) > 0.01) {
                    if (didNotBreakGlass(game, zMove)) {
                        // sound
                        game.getSoundManager().grenadeRebound().at(getCurrentLocation()).pitch(2f).play();

                        // rebound
                        currentVelocity.setZ(-currentVelocity.getZ() * REBOUND_HOR_SPEED_LOSS);

                        // recalculating tiny move with new Z velocity
                        tinyMove = currentVelocity.clone().normalize().multiply(tinyMoveLength);
                    }
                } else {
                    currentVelocity.setZ(0);
                }
            } else {
                // do tiny move in Z direction
                intermediateLoc.add(0, 0, tinyMove.getZ());
            }

            if (UtilBlock.isInsideBlock(intermediateLoc)) {
                MsdmPlugin.severe("Grenade is inside block! (3)");
            }

            // we are inside block, meaning we need to rebound
            if (UtilBlock.isInsideBlock(yMove)) {

                _touchedGround = true;

                if (Math.abs(currentVelocity.getY()) > 0.15) {

                    if (didNotBreakGlass(game, yMove)) {
                        // sound
                        game.getSoundManager().grenadeRebound().at(currentLocation).pitch(2f).play();

                        double newY = -currentVelocity.getY() * REBOUND_VERT_SPEED_LOSS;

                        // cap rebound velocity
                        // this prevents crazy rebound when looking into floor
                        if (newY > 0) {
                            newY = Math.min(newY, 0.5);
                        } else {
                            newY = Math.max(newY, -0.5);
                        }

                        // rebound
                        currentVelocity.setY(newY);

                        // recalculating tiny move with new Y velocity
                        tinyMove = currentVelocity.clone().normalize().multiply(tinyMoveLength);
                    }
                } else {
                    currentVelocity.setY(0);
                }
            } else {
                // do tiny move in Y direction
                intermediateLoc.add(0, tinyMove.getY(), 0);
            }
        }

        Location prevLocation = currentLocation.clone();
        currentLocation = intermediateLoc.clone();

        var pack = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(_entity.getId(),
                (short) (((currentLocation.getX() * 32) -  (prevLocation.getX() * 32)) * 128),
                (short) (((currentLocation.getY() * 32) -  (prevLocation.getY() * 32)) * 128),
                (short) (((currentLocation.getZ() * 32) -  (prevLocation.getZ() * 32)) * 128),
                false
        );

        currentLocation.getWorld().getPlayers().forEach(p -> ((CraftPlayer)p).getHandle().b.sendPacket(pack));
    }
}
