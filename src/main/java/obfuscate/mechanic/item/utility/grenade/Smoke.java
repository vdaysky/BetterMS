package obfuscate.mechanic.item.utility.grenade;

import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.sound.Radio;
import obfuscate.team.StrikeTeam;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.util.block.UtilBlock;
import obfuscate.util.time.Task;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.HashMap;

public class Smoke extends Grenade
{
	public Smoke()
	{
		super(GrenadeType.SMOKE);
	}

	@Override
	public int getMaxStackSize()
	{
		return 1;
	}

	@Override
	public boolean updateCustom(Game game)
	{
		final double fuseTime = 1.5;
		final double liveTime = 15;
		if ((getGroundTicks() > fuseTime * 20 || getLocation().getBlock().getType() == Material.FIRE))
		{
			// setVelocity(new Vector(0,-100,0)); // TODO: why is this here?

			double stay = liveTime * 1000;
			int perBlockDelay = 20;

			final HashMap<Block, Double> blocks = UtilBlock.getInRadius(getLocation(),  4.5d); // 4.5d

			for (final Block block : blocks.keySet())
			{
				double dist = blocks.get(block);

				if (block.getType() == Material.FIRE)
				{
					game.getSoundManager().fireExtinguish().at(getLocation()).play();
					game.getRestore().Restore(block);
				}

				if (block.getType() != Material.AIR && block.getType() != Material.NETHER_PORTAL)
					continue;

				Task smokeSpreadTask = new Task( () -> {
					// if fire appeared - extinguish
					if (block.getType() == Material.FIRE)
					{
						game.getSoundManager().fireExtinguish().at(getLocation()).play();
						game.getRestore().Restore(block);
					}
					game.getRestore().MakeRevertibleChange(
							block,
							Material.NETHER_PORTAL,
							(long) stay + (int)(perBlockDelay * dist)
					); //(_baseTime + blocks.get(block) * _baseTime)

				}, (int) (perBlockDelay - (perBlockDelay * dist))).run();
				game.registerRoundTask(smokeSpreadTask);
			}
			game.registerSmoke(getLocation(), (long) (System.currentTimeMillis() + liveTime * 1000));
			return true;
		}
		
		return false;
	}
	
	@Override
	public void playSound(Game game, StrikePlayer player)
	{
		StrikeTeam team = game.getPlayerRoster(player).getTeam();
		if (team == null)
			return;

		player.playSound(team == StrikeTeam.T ? Radio.T_GRENADE_SMOKE : Radio.CT_GRENADE_SMOKE);
	}

	@Override
	public StrikeItem copy() {
		return new Smoke();
	}
}
