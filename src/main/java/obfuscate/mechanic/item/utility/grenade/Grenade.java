package obfuscate.mechanic.item.utility.grenade;

import obfuscate.game.core.Game;
import obfuscate.game.core.GameInventory;
import obfuscate.game.config.ConfigField;
import obfuscate.game.player.StrikePlayer;
import obfuscate.mechanic.item.StrikeStack;
import obfuscate.mechanic.item.utility.ConsumableItem;
import obfuscate.mechanic.item.utility.grenade.engine.BetterGrenadePhysics;
import obfuscate.mechanic.item.utility.grenade.engine.GrenadePhysics;
import obfuscate.mechanic.item.utility.grenade.engine.OgGrenadePhysics;
import obfuscate.util.chat.C;
import org.bukkit.*;

public abstract class Grenade extends ConsumableItem
{
	protected StrikePlayer _thrower = null;

	protected GrenadeType _type;
	
	protected int _limit;

	private GrenadePhysics physics;

	private int _ticksLived = 0;
	
	public Grenade(GrenadeType stats)
	{
		super(stats);
		_type = stats;
		_limit = stats.getLimit();
	}

	public Location getLocation() {
		return physics.getCurrentLocation().clone();
	}

	public int getGroundTicks() {
		if (physics == null) {
			return 0;
		}
		return physics.getGroundTicks();
	}

	@Override
	public boolean droppable() {
		return true;
	}

	public int getTicksLived() {
		return _ticksLived;
	}

	public void removeEntity() {
		physics.removeEntity();
	}

	@Override
	public boolean canPickup(Game game, StrikePlayer player) {
		GameInventory inventory = game.getGameSession(player).getInventory();
		// todo: move to cfg
		int grenadeLimit = 4;
		int total = inventory.getGrenades().size();

		// total limit and limit for single type not exceeded
		if (total >= grenadeLimit)
			return false;

		StrikeStack grenadeStack = new StrikeStack(3, player);

		// find stack with this grenade type
		for (int i = 3; i < 7; i++) {
			var stack = inventory.getStack(i);

			if (!stack.isEmpty() && stack.top().isAlike(this)) {
				grenadeStack = stack;
			}
		}

		if (grenadeStack.isEmpty())
			return true;

		return grenadeStack.getCount() < getLimit();
	}

	public int getLimit()
	{
		return _limit;
	}

	@Override
	public Integer getSlot() {
		return _type.getSlot();
	}

	public GrenadeType getNadeType()
	{
		return _type;
	}
	
	public void throwGrenade(StrikePlayer player, boolean wasLeftClick, Game game)
	{
		if (game.is(ConfigField.OLD_NADES)) {
			physics = new OgGrenadePhysics();
		} else {
			physics = new BetterGrenadePhysics();
		}

		_thrower = player;

		physics.launch(getStack(), player, wasLeftClick, game);

		game.registerThrownGrenade(this);

		//Sound
		playSound(game, player);
	}
	
	public boolean update(Game game)
	{
		_ticksLived += 1;

		boolean shouldStop = physics.update(game);

		if (shouldStop) {
			return true;
		}

		// Do rebound (alters velocity)
		physics.moveRebounding(game);

		//Custom
		return updateCustom(game);
	}


	public boolean touchedGround() {
		if (physics == null) {
			return false;
		}
		return physics.touchedGround();
	}
	
	public abstract boolean updateCustom(Game game);

	@Override
	public String getShopItemType()
	{
		return C.cDGreen + C.Bold + "Grenade" + ChatColor.RESET;
	}

	public abstract void playSound(Game game, StrikePlayer player);

	public StrikePlayer getThrower() {
		return _thrower;
	}
}
