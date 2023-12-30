package obfuscate.mechanic.item.guns;

import obfuscate.MsdmPlugin;
import obfuscate.event.CustomListener;
import obfuscate.event.bukkit.BulletHitEvent;
import obfuscate.event.bukkit.BulletHitPlayerEvent;
import obfuscate.event.custom.item.gun.bullet.BulletStopEvent;
import obfuscate.event.custom.item.gun.bullet.BulletWallbangEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.player.ClientVersion;
import obfuscate.game.player.StrikePlayer;
import obfuscate.logging.Logger;
import obfuscate.mechanic.version.projectile.FakeEntity;
import obfuscate.util.UtilEffect;
import obfuscate.util.alg.TraceEventV2;
import obfuscate.util.alg.TraceResult;
import obfuscate.util.alg.UtilAlg;
import obfuscate.util.block.UtilBlock;
import obfuscate.util.chat.C;
import obfuscate.util.time.Task;
import de.slikey.effectlib.util.ParticleEffect;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.minecraft.util.MathHelper;
import net.minecraft.world.entity.projectile.EntitySnowball;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;


public class Bullet implements CustomListener
{
	private final StrikePlayer _shooter;

	private @Nullable FakeEntity _entity;
	private final Gun _gun;
	private Location _origin;
	private long LastSound = System.currentTimeMillis() - 100;

	private HashSet<Player> WhizzSound = new HashSet<Player>();
	private final boolean _noScope;

	// wallbang penalty modifier. Unit - wooden blocks
	private double _scaledDistanceThroughBlocks = 0;

	private int ticksLived = 0;

	private Vector _shootDirection;

	private Location _currentLocation;

	private boolean _isInBlock = false;
	private Location _blockEntryLocation;
	private Location _blockPreEntryLocation;

	private final boolean _allowWallbang;
	private final Game _game;

	private boolean _wasInSmoke = false;

	// keep track of players that were hit by this bullet.
	// this way we can make sure that a player is not hit twice by the same bullet,
	// even if player stands on edge of a block or on edge of a game tick.
	private final HashSet<Entity> hitEntities = new HashSet<>();

	public Bullet(Gun gun, StrikePlayer shooter, boolean noScope, boolean allowWallbang)
	{
		_game = shooter.getGame();

		_allowWallbang = allowWallbang;
		_gun = gun;
		_shooter = shooter;
		_noScope = noScope;
	}

//	private Vector calculateFireDirection(StrikePlayer player) {
//		var f = player.getHandle().getXRot();
//		var f1 = player.getHandle().getYRot();
//		float f2 = 0.0F;
//		float f3 = 1.5F;
//		float f4 = 1.0F;
//
//		float f5 = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);
//		float f6 = -MathHelper.sin((f + f2) * 0.017453292F);
//		float f7 = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);
//
//		return new Location(player.getWorld(), (double)f5, (double)f6, (double)f7, f3, f4);
//	}

	private boolean hasEntityAnimation() {
		return !_gun.getGunStats().isSniper();
	}

	public void launch(Vector velocity) {


		//entityliving.locX(), entityliving.getHeadY() - 0.10000000149011612, entityliving.locZ()

		double locX, locY, locZ	;
		if (_game.is(ConfigField.USE_NMS_LAUNCH)) {
			var entityliving = _shooter.getHandle();
			locX = entityliving.locX();
			locY = entityliving.locY() + (double)entityliving.getHeadHeight();
			locZ = entityliving.locZ();

			var yaw = entityliving.getYRot();

			locX -= (double)(MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F);
			locY -= 0.10000000149011612;
			locZ -= (double)(MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F);

//			var yaw = entityliving.getYRot();
//
//			locX -= MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F * 0.16F;
//			locY -= 0.10000000149011612D;
//			locZ -= MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F * 0.16F;

//			locX = entityliving.locX();
//			locY = entityliving.getHeadY() - 0.10000000149011612;
//			locZ = entityliving.locZ();

			if (_shooter.isSneaking()) {
				// compensate for 1.14 sneaking on older versions
				if (_shooter.getVersion() < ClientVersion.V1_14.getProtocolVersion()) {
					locY += 0.5;
					locY -= 0.15; // apply 1.8 sneaking
				}
			}

			_origin = new Location(
					_shooter.getWorld(),
					locX, locY, locZ
			);
		} else {
			 locX = _shooter.getLocation().getX();
			 locY = _shooter.getLocation().getY() + 1.7; // - 0.10000000149011612D?
			 locZ = _shooter.getLocation().getZ();

			if (_shooter.isSneaking()) {
				if (_shooter.getVersion() < ClientVersion.V1_14.getProtocolVersion()) {
					locY -= 0.15;
				} else {
					locY -= 0.5;
				}
			}

			Vector look = _shooter.getLocation().getDirection();
			Vector right = new Vector(-look.getZ(), 0, look.getX()).normalize();

			_origin = new Location(
					_shooter.getWorld(),
					locX, locY, locZ
			)
			.add(right.clone().multiply(0.1))
			.add(look.clone().multiply(0.5));
		}



//		locX -= MathHelper.cos(yaw / 180.0F * 3.1415927F) * 0.16F * 0.16F;
//		locY -= 0.10000000149011612D;
//		locZ -= MathHelper.sin(yaw / 180.0F * 3.1415927F) * 0.16F * 0.16F;




		_origin.getWorld().spawnParticle(Particle.SMOKE_NORMAL, _origin, 0, 0, 0, 0, 1);

		_shootDirection = velocity;
		_currentLocation = _origin.clone();

		if (hasEntityAnimation()) {
			_entity = new FakeEntity(EntitySnowball.class, _origin);
			_entity.spawn();
		}

		boolean spawnedInBlock = UtilBlock.isInsideBlock(_origin);

		if (spawnedInBlock) {
			boolean shouldStop = handleBulletEnterBlock(_origin, _origin);

			if (shouldStop) {
				return;
			}
		}

		// do one update instantly
		if (updateV2()) {
			new BulletStopEvent(this).trigger();
			return;
		}

		// do more updating for slow bullets
		if (!isInstant()) {
			_scheduleUpdate();
		} else {
			new BulletStopEvent(this).trigger();
		}
	}
	private void _scheduleUpdate() {
		new Task(()-> {
			boolean shouldStop = updateV2();
			if (!shouldStop && !shouldStop()) {
				_scheduleUpdate();
			} else {
				new BulletStopEvent(this).trigger();
			}
		}, 1).run();
	}


	public Game getGame() {
		return _game;
	}


	//Tracer Particle
	public void playTracer(Location hitLocation)
	{
		UtilEffect.showLine(_origin, hitLocation, ParticleEffect.CRIT, 1);
	}

	/** removes entity representing bullet and mark bullet to be deleted */
	public void remove() {
		if (_entity != null) {
			_entity.remove();
		}
	}

	/** Calculate a value in range from 0 to 1 representing how much damage will be preserved after
	 * a wallbang this bullet performed.
	 *
	 * @return value in range from 0 to 1 which can be used to multiply damage by
	 * */
	public double getWallbangPenalty() {

		double damageBlockFactor = 1 - (getScaledWallbangDistance() / _gun.getMaxWallbangDist());

		if (damageBlockFactor < 0.1) {
			return 0;
		}

//		final double Beta = 3.3D;
//		// scale damage percentage a bit, adds a threshold to minimal damage. Could be a bad idea lol
//		// btw this is a really nice sigmoid, should save it for future use
//		damageBlockFactor = 1 - (1D / ( 1D + Math.pow(damageBlockFactor / (1D - damageBlockFactor), -Beta)));
		return damageBlockFactor;
	}
	/** Register bullet going through material
	 *
	 * @param blockMod block penetration modifier
	 * @param dist distance travelled through block
	 * */
	public void addPenetrationMod(double blockMod, double dist) {
		_scaledDistanceThroughBlocks += blockMod * dist;
	}

	private boolean handleBulletEnterBlock(Location preLocation, Location location) {
		if (_game.getConfig().getValue(ConfigField.SHOW_IMPACTS).bool()) {
			Plugin p = MsdmPlugin.getInstance();
			HolographicDisplaysAPI api = HolographicDisplaysAPI.get(p);
			Hologram hologram = api.createHologram(location);
			hologram.getLines().appendText(C.cGreen + Math.round((getDamage() * 100D) / 100D) + " Damage");
			new Task(hologram::delete, 20 * 10).run();
		}

		// instantly stop if we hit a wall.
		// this shouldn't cause any issues because players
		// are traced first, so if any players should've been hit
		// they probably were already
		if (!_allowWallbang) {
			if (_entity != null) {
				_entity.teleport(location);
				_entity.remove();
				_entity = null;
			}
			_currentLocation = location;
			// bullet hit wall
			new BulletHitEvent(_game, this, location, preLocation, null).trigger();
			return true;
		}

		_isInBlock = true;
		_blockEntryLocation = location;
		_blockPreEntryLocation = preLocation;
		if (_entity != null) {
			_entity.teleport(preLocation);
		}
		_currentLocation = preLocation;

		// bullet hit wall
		new BulletHitEvent(_game, this, location, preLocation, null).trigger();
		return false;
	}

	private boolean updateV2() {

		ticksLived++;

		// if bullet lived for 10 seconds stop updates
		if (ticksLived > 20 * 10) {
			return true;
		}

		// direction of bullet of length of 1 block
		Vector normalDirection = _shootDirection.clone().normalize();

		// list of things that will happen during this tick
		TraceResult traceResult;

		Location start = getLocation().clone();

		// whether to hit past player location
		int retrospectTicks = _game.get(ConfigField.RETROSPECT_HITREG);

		// try to hit bullet at between two tick locations of a bullet
		traceResult = UtilAlg.traceTickV2(
			start.clone(),
			normalDirection.clone(),
			_shootDirection.length(),
			_game.getPlayerHitbox(this),
			retrospectTicks,
			hitEntities
		);

		// record close players to bullet trajectory in this tick
		if (_game.is(ConfigField.DEBUG_HITREG)) {

			for (StrikePlayer p : UtilAlg.closePlayersToTrajectory(
					start.clone(),
					normalDirection.clone(),
					_shootDirection.length())
			) {
				_game.getHitRegLog().getBulletLog(this).addClosePlayer(
						p,
						p.getLocation(),
						UtilAlg.lineToPointDistance(
								start.toVector(),
								start.clone().add(_shootDirection).toVector(),
								p.getLocation().toVector()
						)
				);
			}
		}

		// show all bullet events in order
		if (_game.getConfig().getValue(ConfigField.DEBUG_BULLET).bool()) {
			printTraceReport(traceResult.getEvents());
		}

		// now we have to understand events that happened during trace
		for (TraceEventV2 trace : traceResult.getEvents()) {

			if (trace.blockEvent()) {
				// generally if isInBlock is false we shouldn't get come out event
				// and if isInBlock is true we shouldn't get a hit event
				if ((!_isInBlock && trace.isEntry())) {

					boolean shouldStop = handleBulletEnterBlock(trace.getPreLocation(), trace.getLocation());

					if (shouldStop) {
						return true;
					}

				}
				else if (_isInBlock && trace.isExit()) {
					_isInBlock = false;

					double wallbangDistance = trace.getLocation().clone().distance(_blockEntryLocation);

					double penMod = (UtilBlock.getBlockPenetrationModifier(_blockEntryLocation.getBlock().getType()) +
							UtilBlock.getBlockPenetrationModifier(trace.getPreLocation().getBlock().getType())) / 2;

					// add to penalty based on what blocks we penetrate
					addPenetrationMod(penMod, wallbangDistance);

					// trigger BulletWallbangEvent
					boolean wallbangCancelled = new BulletWallbangEvent(
							_game,
							this,
							_blockEntryLocation,
							_blockPreEntryLocation,
							trace.getLocation(),
							trace.getPreLocation()
					).triggerSync();

					// stop updates
					if (wallbangCancelled) {
						if (_entity != null) {
							_entity.teleport(_blockEntryLocation);
							_entity.remove();
						}
						_currentLocation = _blockEntryLocation;
						return true;
					} else {
						if (_game.getConfig().getValue(ConfigField.SHOW_IMPACTS).bool()) {
							Plugin p = MsdmPlugin.getInstance();
							HolographicDisplaysAPI api = HolographicDisplaysAPI.get(p);
							Hologram hologram = api.createHologram(trace.getLocation().add(new Vector(0, 0.5, 0)));
							hologram.getLines().appendText(C.cRed + (Math.round(wallbangDistance * 100D) / 100D) + " Blocks " + Math.round((getDamage() * 100D) / 100D) + " Damage");
							new Task(hologram::delete, 20 * 10).run();
						}
					}
					if (_entity != null) {
						_entity.respawn();
					}
					_blockEntryLocation = null;
					_blockPreEntryLocation = null;
					_currentLocation = trace.getLocation();
				}
			}
			else if (trace.hitEntity()) {
				Entity ent = trace.getEntity();

				// bullet hit entity
				if (_game.getConfig().getValue(ConfigField.SHOW_IMPACTS).bool()) {
					Plugin p = MsdmPlugin.getInstance();
					HolographicDisplaysAPI api = HolographicDisplaysAPI.get(p);
					Hologram hologram = api.createHologram(trace.getLocation());
					hologram.getLines().appendText(C.cRed + "Hit Entity " + Math.round((getDamage() * 100D) / 100D) + " Damage");
					new Task(hologram::delete, 20*10).run();
				}

				// move bullet to pre hit location, so we can trace it from there in triggered events\
				if (_entity != null) {
					_entity.teleport(trace.getPreLocation());
				}
				_currentLocation = trace.getLocation();

				if (ent instanceof Player player) {

					if (player == _shooter.getPlayer()) {
						// player hit himself
						continue;
					}

					StrikePlayer strikePlayer = StrikePlayer.getOrCreate(player);
					// bullet hit player
					new BulletHitPlayerEvent(
							_game,
							this,
							trace.getLocation(),
							trace.getPreLocation(),
							strikePlayer,
							trace.getPlayerLocation(),
							trace.getHitArea()
					).trigger();
				} else {
					// bullet hit non-player entity
					new BulletHitEvent(_game, this, trace.getLocation(), trace.getPreLocation(), ent).trigger();
				}
			}

			if (trace.isSmoke()) {
				_wasInSmoke = true;
			}
		}

		// get location where we left off
		Location nextLocation = traceResult.getLastLocation();

		if (_entity != null) {
			_entity.teleport(nextLocation);
		}
		_currentLocation = nextLocation;

		return false;
	}

	private static void printTraceReport(ArrayList<TraceEventV2> report) {
		int i = 0;
		Logger.info("Trace report Started");
		for (TraceEventV2 trace : report) {
			Logger.info("========== Tick " + i + " ==========");
			String preLoc = trace.getPreLocation().getX() + " " + trace.getPreLocation().getY() + " " + trace.getPreLocation().getZ();
			String loc = trace.getLocation().getX() + " " + trace.getLocation().getY() + " " + trace.getLocation().getZ();

			if (trace.hitEntity()) {
				Logger.info(i + ". Entity hit. Right before: " + preLoc + " right after: " + loc);
			}

			if (trace.isSmoke()) {
				Logger.info(i + ". Smoke hit. Right before: " + preLoc + " right after: " + loc);
			}

			if (trace.blockEvent()) {
				if (trace.isEntry()) {
					Logger.info(i + ". Come in block " + trace.getLocation().getBlock().getType() + ". Right before: " + preLoc + " right after: " + loc);
				}
				if (trace.isExit()) {
					Logger.info(i + ". Come out of a block " + trace.getPreLocation().getBlock().getType() + ". Right before: " + preLoc + " right after: " + loc);
				}
			}

			i += 1;
		}
		Logger.info("Trace report Ended");
	}

	/** this value is a scaled distance travelled through blocks by bullet.
	 * multiply it by gun block penetration, and we will get damage modifier
	 */
	public double getScaledWallbangDistance() {
		return _scaledDistanceThroughBlocks;
	}

	public StrikePlayer getShooter() {
		return _shooter;
	}

	public Location getLocation() {
		return _currentLocation;
	}

	private boolean shouldStop() {
		return ticksLived > 80 || _origin.clone().distance(getLocation()) > 200;
	}

	public boolean wasNoScope()
	{
		return _noScope;
	}
	public boolean isInstant() {
		return _gun.getGunType() == GunType.SNIPER;
	}

	public double getRawDamage() {
		return _gun.getDamage();
	}

	public double getDamage() {
		return (getRawDamage() + getDamageDropOff(_currentLocation)) * getWallbangPenalty();
	}

	public double getDamageDropOff(Location destination) {
		return Math.max(-_gun.getDamage() + 0.5, -_gun.getDamage() * (_gun.getDropOff() * _origin.distance(destination)));
	}

	public Location getOrigin() {
		return _origin;
	}

	public Gun getGun() {
		return _gun;
	}

	public Vector getDirection() {
		return _shootDirection;
	}

	public boolean wasInSmoke() {
		return _wasInSmoke;
	}
}
