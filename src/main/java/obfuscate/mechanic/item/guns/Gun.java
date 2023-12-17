package obfuscate.mechanic.item.guns;


import obfuscate.event.LocalEvent;
import obfuscate.event.custom.item.gun.GunShootEvent;
import obfuscate.event.custom.item.gun.ReloadEndEvent;
import obfuscate.event.custom.item.gun.ReloadStartEvent;
import obfuscate.event.custom.item.ItemFocusEvent;
import obfuscate.event.custom.item.ItemLostFocusEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.core.IGame;
import obfuscate.game.debug.BulletLog;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.version.*;
import obfuscate.util.chat.C;
import obfuscate.util.recahrge.IRecharge;
import obfuscate.util.recahrge.ItemRecharge;
import obfuscate.util.time.Task;
import obfuscate.util.time.Time;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Gun extends StrikeItem
{
	protected GunStats _gunStats;
	protected int _slot;
	protected double _cone;

	protected int bullets_left;
	protected int _spareAmmo;

	// used by reload animation
	Long reloadStarted = null;
	boolean reloading = false;
	private Task reloadTask;

	// custom shoot hook
	private int sprayPatternPos = 0;
	private long lastShotMs = 0;

	private final IRecharge<StrikeItem> recharge = new ItemRecharge();

	public Gun(GunStats gunStats)
	{
		super(gunStats);

		_gunStats = gunStats;
		bullets_left = gunStats.getClipSize();
		_slot = gunStats.getItemType().getSlot();
		_cone = gunStats.getConeMin();
		_spareAmmo = gunStats.getClipReserve() * getClipSize();
	}

	@Override
	public boolean droppable() {
		return true;
	}

	public void reload(Game game, StrikePlayer holder, boolean instant) {
		reload(game, holder, instant, false);
	}

	public void refill() {
		_spareAmmo = _gunStats.getClipReserve() * _gunStats.getClipSize();
		bullets_left = _gunStats.getClipSize();
	}

	public void reload(Game game, StrikePlayer holder, boolean instant, boolean silent)
	{
		if (bullets_left == getClipSize())
			return;

		boolean	hasInfiniteSpare = game.get(ConfigField.INFINITE_AMMO) == 2 || game.get(ConfigField.INFINITE_AMMO) == 1;

		if (_spareAmmo == 0 && !hasInfiniteSpare)
			return;

		if (instant)
		{
			if (!reloading)
				new ReloadStartEvent(game, this, holder, true).trigger();
			new ReloadEndEvent(game, this, holder, ReloadEndEvent.ReloadEndReason.SUCCESS, !reloading, silent).trigger();

			reloading = false;

			int toRefill = Math.max(0, getClipSize() - bullets_left);
			int hasToRefill = Math.min(_spareAmmo, toRefill);

			_spareAmmo = hasInfiniteSpare ? (_gunStats.getClipReserve() * _gunStats.getClipSize() ) : _spareAmmo - hasToRefill;
			bullets_left += hasToRefill;

			game.getGameSession(holder).getHotbarMessenger().setMessage(printBulletsState());
		}
		else
		{
			if (reloading)
				return;

			reloadStarted = System.currentTimeMillis();
			reloading = true;
			new ReloadStartEvent(game, this, holder, false).trigger();

			reloadTask = new Task(()->{
				// but if we cancel and start reload task will success
				// that's why i cancel task on event
				if (!reloading) {
					return;
				}
				reload(game, holder, true);
				}, (int) (getReloadTime()/50));

			game.registerRoundTask(reloadTask.run());
		}
	}

	public void soundRefire(Location loc) {

	}

	@LocalEvent
	private void onReloadStart(ReloadStartEvent e)
	{
		if ( !e.isInstant() ) {
			e.getGame().getSoundManager().startReload().at(e.getHolder().getLocation()).soundDistance(16).play();
		}

		e.getGame().getGameSession(e.getHolder()).getHotbarMessenger().importantMessage(
				aVoid->e.getGun().getReloadState(), (int) (e.getGun().getReloadTime()), 1);
	}

	@LocalEvent
	private void onReloadEnd(ReloadEndEvent e)
	{
		if (e.getHolder().getGunInHand(e.getGame()) == this)
			e.getGame().getGameSession(e.getHolder()).getHotbarMessenger().setMessage(printBulletsState());

		if (e.isSilent())
			return;

		if ( !e.isInstant() ) {
			e.getGame().getSoundManager().endReload().at(e.getHolder().getLocation()).play();
		}

		if (e.getReason() == ReloadEndEvent.ReloadEndReason.SWITCH)
			e.getGame().getGameSession(e.getHolder()).getHotbarMessenger().importantMessage(
					C.cRed + C.Bold + e.getGun().getName() + " Reload Cancelled", 2500);
	}

	public boolean hasBullets()
	{
		return bullets_left > 0;
	}
        
//	public void shoot(StrikePlayer holder, Game game)
//	{
//		if (!hasBullets())
//		{
//			reload(game, holder, false);
//			return;
//		}
//
//		if (reloading)
//			return;
//
//		// for some unknown reason scheduler is broken. number of ticks
//		// actually has to be increased by 1 manually
//
//		//Standard (300) RPM
//		shootOnce(holder, game);
//
//		//600RPM
//		if (_gunStats.getFireRate() <= 100 && _gunStats.getFireRate() > 50)
//		{
//			new Task(
//					()->shootOnce(holder, game),
//					2+1
//			).run();
//		}
//
//		//1200RPM
//		if (_gunStats.getFireRate() <= 50)
//		{
//			for (int i=1 ; i<4 ; i++)
//			{
//				new Task(
//						()->shootOnce(holder, game),
//						i+1
//				).run();
//			}
//		}
//	}

	public void shootOnce(StrikePlayer holder, Game game)
	{
		if (reloading)
			return;

		if (!recharge.done(this, "shoot", getFireRate()))
			return;

		//Visual
//		Location loc = holder.getEyeLocation().add(holder.getLocation().getDirection().multiply(1.2));
//		loc.add(UtilAlg.getRight(holder.getLocation().getDirection()).multiply(0.5));
//		loc.add(UtilAlg.getDown(holder.getLocation().getDirection()).multiply(0.3));
//		Location loc = calculateFireLocation(holder);

//		UtilParticle.PlayParticle(ParticleEffect., loc, 0, 0, 0, 0, 1);

		Bullet bullet = fireBullet(game, holder);

		boolean cancelled = new GunShootEvent(holder, game, this, bullet).triggerSync();

		if (cancelled) {
			bullet.remove();
			return;
		}

		lastShotMs = System.currentTimeMillis();

		if (!game.getConfig().getValue(ConfigField.INFINITE_AMMO).eq(1))
		{
			bullets_left--;
			game.getGameSession(holder).getHotbarMessenger().setMessage(printBulletsState());
		}


		//Effect
		soundFire(holder.getLocation());

		if (_gunStats.getGunType() == GunType.SNIPER)
			game.getGameSession(holder).setScoped(false);

		if (!hasBullets())
		{
			reload(game, holder, false);
		}
	}

	// Proxy calls to core hook
	public void shoot(StrikePlayer holder, Game game) {

		if (!hasBullets()) {
			reload(game, holder, false);
			return;
		}

		if (isReloading())
			return;

		// for some unknown reason scheduler is broken. number of ticks
		// actually has to be increased by 1 manually

		int delay = 0;
		//Standard (300) RPM

		// I don't know why, I don't want to know why
		// but this at least works.
		// I am in no mood to try and figure those timings out
		if (getGunStats().isSniper()) {
			shootOnce(holder, game);
		} else {
			new Task(()-> shootOnce(holder, game), delay).run();
		}
		//600RPM
		if (getFireRate() <= 100 && getFireRate() > 50)
		{
			new Task(
					()->shootOnce(holder, game),
					2 + 1 + delay
			).run();
		}

		//1200RPM
		if (getFireRate() <= 50)
		{
			for (int i=1 ; i<4 ; i++)
			{
				new Task(
						()->shootOnce(holder, game),
						i +1 + delay
				).run();
			}
		}
	}

	// Proxy calls to core hook
	public Bullet fireBullet(Game game, StrikePlayer player) {

		final int instantBulletSpeed = 200;
		final int slowBulletSpeed = 6;


		//Instant?
		GunStats stats = getGunStats();
		if (stats == null) return null;

		boolean instant = (stats.getGunType() == GunType.SNIPER);

		// COF

		Vector recoil = player.getEyeLocation().getDirection();

		Vector horizontal = new Vector(recoil.getZ(), 0, -recoil.getX()).normalize();
		Vector vertical = recoil.clone().crossProduct(horizontal).normalize();

		// multiply by -1 to ensure negative values go to the left and positive to the right
		horizontal.multiply(-1);

		// Calculate spray pattern offsets
		RecoilOffset offset;

		if (game.is(ConfigField.NO_RECOIL)) {
			offset = new RecoilOffset(0, 0);
		} else {
			offset = getSprayPosition();
			increaseSprayPatternPos();
		}

		// apply offsets to recoil, those are intended and bot random-based
		Vector relativeRecoil = horizontal.clone().multiply( offset.getHor() );
		relativeRecoil.add(vertical.clone().multiply( offset.getVert() ));

		final double RECOIL_IMPACT = 1; // 0.0275; // 0.020 too low // 0.035 too high

		// scale for debug purposes
		relativeRecoil.multiply(RECOIL_IMPACT);

		// now recoil is affected by gun pattern offset
		recoil.add( relativeRecoil );

		// get player's inaccuracy
		double playerCone = getCone(game, player);

		// introduce randomness
		Vector randomness;

		if ( !game.is(ConfigField.NO_SPREAD) ) {
			final double RANDOMNESS_IMPACT = 1;

			randomness = new Vector(Math.random()-0.5, Math.random()-0.5, Math.random()-0.5).normalize();
			randomness.multiply(RANDOMNESS_IMPACT * playerCone);
		}
		else {
			randomness = new Vector(0, 0, 0);
		}

		// scale recoil direction to affect shoot direction a little
		recoil.normalize();

		// apply randomness
		recoil.add(randomness);

		recoil.normalize();

		double newCone = Math.min(
				getGunStats().getConeMax(),
				getCone() + getGunStats().getConeIncreaseRate() * 0.5
		);

		float coneModifier = game.get(ConfigField.CONE_MODIFIER) / 100f;

		setCone(newCone * coneModifier); // scale a bit, originally not there

		//Velocity
		Vector velocity = recoil.multiply(instant ? instantBulletSpeed : slowBulletSpeed);

		boolean isNoScope = !player.scoped(game) && getGunStats().isSniper();
		boolean allowWallbangs = game.is(ConfigField.ALLOW_WALLBANG);

		//Shoot
		Bullet bullet = new Bullet(this, player, isNoScope, allowWallbangs);

		// record bullet in log as soon as possible
		// (before launch, because it will try to write logs instantly, log instance should exist by that time)
		if (game.is(ConfigField.DEBUG_HITREG)) {
			game.getHitRegLog().addBulletLog(
				new BulletLog(
					bullet,
					player.getLocation(),
					System.currentTimeMillis()
				)
			);
		}

		bullet.launch(velocity);

		return bullet;
	}
	
	public double getArmorPenetration()
	{
		return _gunStats.getArmorPen();
	}

	public double getCone() {
		return _cone;
	}

	public double getCone(Game game, StrikePlayer player)
	{
		double cone = _cone;

		//Airborne Penalty
		if (!player.getPlayer().isOnGround())
		{
			cone += _gunStats.getGunType().getJumpPenalty(); // added * 4
		}
		
		//Sprint Penalty
		else if (player.getPlayer().isSprinting())
		{
			cone += _gunStats.getGunType().getSprintPenalty();
		}

		//Move Penalty
		else if (!Time.elapsed(player.getLastMoveTime(game), 75))
		{
			cone += player.getLastMoveDistance(game) * _gunStats.getGunType().getMovePenalty();
		}

		//Speed penalty
//		double playerSpeed = player.getVelocity().length();
//		if (playerSpeed > 0.1 && game.is(ConfigField.APPLY_SPEED_PENALTY)) {
//			double coneDelta = Math.pow(playerSpeed, 2) * _gunStats.getGunType().getSpeedPenalty();
//			//Logger.print("add " + coneDelta);
//			cone += coneDelta;
//		}

		//Crouch
		else if (player.getPlayer().isSneaking() && _gunStats.getGunType() != GunType.SNIPER)
			cone = cone * 0.7; // 0.8

		float scopingProgress = Math.min(1, game.getGameSession(player).getScopedDuration() / (float) game.get(ConfigField.FULL_SCOPE_DELAY));

		//Sniper Zoomed
		if (scopingProgress == 1 && _gunStats.getScope() && player.scoped(game))
		{
			//Snipers Perfectly Accurate if not jumping
			if (_gunStats.getGunType() == GunType.SNIPER)
			{
				cone = 0;
				
				//Airborne Penalty
				if (!player.getPlayer().isOnGround())
					cone += _gunStats.getGunType().getJumpPenalty(); // added * 4
			}
			//25% Recoil Decrease
			else
			{
				cone = cone * 0.75;
			}
		}

		// if player is scoped and grounded, he will gather accuracy
		else if (player.scoped(game) && player.getPlayer().isOnGround()) {
			float qsAccuracyMod = (game.get(ConfigField.QS_ACCURACY) / 100f);
			cone *= (1 - scopingProgress) / qsAccuracyMod;
		}

		return cone;
	}

	public void cancelReload(IGame game, StrikePlayer holder, ReloadEndEvent.ReloadEndReason reason)
	{
		if (!reloading)
			return;

		reloadTask.cancel();
		new ReloadEndEvent(game,this, holder, reason, false, false).trigger();
		reloading = false;
	}

	public String getReloadState()
	{
		if (reloading)
		{
			//▯
			int bars = 24;
			float elapsed = System.currentTimeMillis() - reloadStarted;
			double p = elapsed/getReloadTime();

			if (p > 1)p = 1;
			StringBuilder res = new StringBuilder(C.cGreen + C.Bold);

			for (int bar = 0; bar < bars * p; bar++)
			{
				res.append("▌");
			}
			res.append(C.cRed + C.Bold);
			for (int bar = 0; bar < bars - (bars * p); bar++)
			{
				res.append("▌");
			}
			double left = reloadStarted + getReloadTime() - System.currentTimeMillis();
			double d = Math.round((left / 1000) * 10) / 10.0;
			return C.cWhite + C.Bold + getName() +  " Reload: " + res + " " + C.cWhite + d + " Seconds";
		}
		return null;
	}

	public String printBulletsState()
	{
		if (bullets_left == 0 && _spareAmmo == 0) {
			return C.cRed + "No Ammo";
		}
		return C.cGreen + bullets_left + C.cWhite + " / " +  C.cYellow + _spareAmmo;
	}

	public long getReloadTime()
	{
		return _gunStats.getReloadTime();
	}
	public int getClipSize()
	{
		return _gunStats.getClipSize();
	}

	public double getDropOff()
	{
		return _gunStats.getDropoff();
	}

	public void soundFire(Location loc)
	{
		loc.getWorld().playSound(loc, _gunStats.getFireSound(), _gunStats.getGunType().getVolume(), (float)(Math.random() * 0.2 + 0.9));
	}

	public void reduceCone()
	{
		// this added amount of ticks is totally random, no idea what's going on with it
		if (lastShotMs + getFireRate() + 40 < System.currentTimeMillis()) {
			// if not spraying reduce spray pattern
//			if (System.currentTimeMillis() - lastShotMs < 3000) {
//				MsdmPlugin.logger().info((System.currentTimeMillis() - lastShotMs) + " MS since last shot, reduce spray. Fire rate: " + getFireRate());
//			}


			double ticks_between_shots = getFireRate() / 50d;
			double shots_per_tick = 1 / ticks_between_shots;
			int reduce_shots_per_tick = (int) Math.ceil(shots_per_tick * 3);
			decreaseSprayPatternPos(reduce_shots_per_tick);
		}

//		if (System.currentTimeMillis() - lastShotMs < getFireRate()) {
//
//			return;
//		}
		if (lastShotMs + getFireRate() + 49 > System.currentTimeMillis()) {
//			MsdmPlugin.highlight("Do not decrease cone, spraying");
			return;
		}

		// if player shot less than *fire rate* MS ago do not decrease cone
		if (_cone == _gunStats.getConeMin()) {
			return;
		}



		_cone = Math.max(_gunStats.getConeMin(), _cone - (_gunStats.getConeReduceRate() / 20d)); // og
//		MsdmPlugin.highlight("Decrease cone to " + _cone);

//		_cone = Math.max(_gunStats.getConeMin(), _cone - (_gunStats.getConeReduceRate() / 40d));

	}

	public double getDamage()
	{
		return _gunStats.getDamage() / 5d;
	}

	public boolean hasScope()
	{
		return _gunStats.getScope();
	}

	public GunType getGunType()
	{
		return _gunStats.getGunType();
	}

	public long getFireRate()
	{
		return _gunStats.getFireRate();
	}

	@LocalEvent
	public void onFocusLost(ItemLostFocusEvent e)
	{
		if (reloading)
		{
			switch (e.getReason())
			{
				case DROP:
					cancelReload(e.getGame(), e.getHolder(), ReloadEndEvent.ReloadEndReason.DROP);
					break;
				case SWITCH:
					cancelReload(e.getGame(), e.getHolder(), ReloadEndEvent.ReloadEndReason.SWITCH);
					break;
			}
		}
		else
		{
			e.getGame().getGameSession(e.getHolder()).getHotbarMessenger().setMessage("");
		}
		e.getGame().getGameSession(e.getHolder()).getHotbarMessenger().clearMessage();
	}

	@LocalEvent
	public void onFocus(ItemFocusEvent e)
	{
		e.getGame().getGameSession(e.getHolder()).getHotbarMessenger().setMessage(printBulletsState());
	}

    public int getKillReward()
	{
		return _gunStats.getKillReward();
    }

	@Override
	public StrikeItem copy() {
		return new Gun((GunStats) getStats());
	}

	public boolean isReloading() {
		return reloading;
	}

	public GunStats getGunStats() {
		return _gunStats;
	}

	public void setCone(double cone) {
		_cone = cone;
	}

	public void increaseSprayPatternPos() {
		sprayPatternPos = (sprayPatternPos + 1) % _gunStats.getSprayPattern().size();
	}

	public void decreaseSprayPatternPos(int decrBy) {

		sprayPatternPos = Math.max(0, sprayPatternPos - decrBy);
	}

	public RecoilOffset getSprayPosition() {
		return getGunStats().getSprayPattern().get(sprayPatternPos);
	}

	/** value ranged from 0 to 1, representing how much of original damage will remain after 1-block wallbang */
	public double getMaxWallbangDist() {
		return getGunStats().getMaxWallbangDist();
	}
}
