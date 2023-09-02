package obfuscate.mechanic.item.utility.grenade;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.sound.Radio;
import obfuscate.team.StrikeTeam;
import obfuscate.util.block.UtilBlock;
import obfuscate.util.UtilParticle;
import obfuscate.util.time.Task;
import obfuscate.util.time.Time;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashMap;

public abstract class FireGrenadeBase extends Grenade
{
	private final long _baseTime;
	
	public FireGrenadeBase(GrenadeType type, long burnTime)
	{
		super(type);
		_baseTime = burnTime;
	}

	@Override
	public int getMaxStackSize()
	{
		return 1;
	}

	@Override
	public Integer getSlot() {
		return null;
	}

	@Override
	public boolean updateCustom(Game game)
	{
		if (touchedGround())
		{
			createFire(game, getLocation());
			return true;
		}
		return false;
	}

	private void createFire(final Game game, final Location loc)
	{
		//Fire Blocks
		final HashMap<Block, Double> blocks = UtilBlock.getInRadius(loc, 3.5d);

		//Sound
		game.getSoundManager().fireStart().at(loc).play();

		//Particle
		UtilParticle.PlayParticle(ParticleEffect.LAVA, loc.add(0, 0.2, 0), 0.3f, 0f, 0.3f, 0, 30);

		for (final Block block : blocks.keySet())
		{
			Block baseBlock = block.getRelative(BlockFace.DOWN);

			// Extinguish fire by nearby smoke blocks
			if (block.getType() == Material.NETHER_PORTAL)
			{
				game.getSoundManager().fireExtinguish().at(loc).play();
				continue;
			}

			//Edited by TheMineBench, to keep the two-half-slabs from burning
//			if (block.getType() != Material.AIR && !block.getType().name().toLowerCase().contains("step") ||  block.getType().name().toLowerCase().contains("double"))
//				continue;

			// fire requires solid block below
			if (!UtilBlock.solid(baseBlock)) {
				continue;
			}

			// TODO: nicer solution for block checking

			// fire can only replace transparent block or a bottom slab or another fire block or carpet
			if (!UtilBlock.airFoliage(block) && !UtilBlock.isBottomSlab(block) && !UtilBlock.isCarpet(block) && block.getType() != Material.FIRE) {
				continue;
			}

			Task fireSpreadTask = new Task( () -> {
				// if smoke block appeared - don't spread fire
				if (block.getType() == Material.NETHER_PORTAL)
				{
					game.getSoundManager().fireExtinguish().at(loc).play();
					return;
				}
				// spread fire everywhere except for smoke
				game.getRestore().MakeRevertibleChange(
						block,
						Material.FIRE,
						(long) (_baseTime + blocks.get(block) * _baseTime)
				);

				// save grenade that owns block of fire
				game.registerFireBlock(block, this, Time.now() + (long) (_baseTime + blocks.get(block) * _baseTime));

			}, 60 - (int)(60d * blocks.get(block))).run();
			game.registerRoundTask(fireSpreadTask);
		}
		
		//Initial Burn Sound
		Task fireSoundTask = new Task(() -> game.getSoundManager().burn().at(loc), 20).run();
		game.registerRoundTask(fireSoundTask);

		//Register
		game.registerIncendiary(loc, System.currentTimeMillis() + (_baseTime*2-4000));
	}
	
	@Override
	public void playSound(Game game, StrikePlayer player)
	{
		StrikeTeam team = game.getPlayerRoster(player).getTeam();
		if (team == null)
			return;

		player.playSound(team == StrikeTeam.T ? Radio.T_GRENADE_FIRE : Radio.CT_GRENADE_FIRE);
	}
}
