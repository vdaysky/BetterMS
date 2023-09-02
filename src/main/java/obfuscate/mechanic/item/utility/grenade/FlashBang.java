package obfuscate.mechanic.item.utility.grenade;

import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.sound.Radio;
import obfuscate.team.StrikeTeam;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.util.*;
import obfuscate.util.alg.UtilAlg;
import obfuscate.util.block.UtilBlock;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class FlashBang extends Grenade
{

	public FlashBang() {
		super(GrenadeType.FLASH);
	}

	@Override
	public int getMaxStackSize()
	{
		return 2;
	}

	@Override
	public boolean updateCustom(Game game)
	{
		if (getTicksLived() > 40)
		{
			FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.WHITE).with(Type.BALL_LARGE).trail(false).build();
			UtilFirework.playFirework(getLocation().add(0, 0.5, 0), effect);
			
			HashMap<StrikePlayer, Double> players = UtilPlayer.getInRadius(getLocation(), 48);
			for (StrikePlayer player : players.keySet())
			{
				if (!player.isAlive(game))
					continue;
				
				//Line of Sight
				Location loc = player.getEyeLocation();
				
				boolean sight = true;

				while (UtilMath.offset(loc, getLocation()) > 0.5)
				{
					if (UtilBlock.isInsideBlock(loc) && UtilBlock.solid(loc.getBlock()))
					{
						sight = false;
						break;
					}
					
					loc.add(UtilAlg.getTrajectory(loc, getLocation()).multiply(0.2));
				}
				
				if (!sight)
					continue;
				
				//Calculate if player is looking away
				Location eyeToGrenade = player.getEyeLocation().add(UtilAlg.getTrajectory(player.getEyeLocation(), getLocation()));
				double flashIntensity = 2 - UtilMath.offset(player.getEyeLocation().add(player.getLocation().getDirection()), eyeToGrenade);

				float baseDuration = game.get(ConfigField.FLASH_BASE_DURATION) / 100f;
				float addedDuration = game.get(ConfigField.FLASH_PROXIMITY_ADDED_DURATION) / 100f;
				//Duration
				double duration = (baseDuration + (addedDuration * (players.get(player)))) * flashIntensity;
//				duration += 1;
				//Blind
				player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int)duration * 20, 1, false, false));
				//game.Manager.GetCondition().Factory().Blind(getName(), player, _thrower, duration, 0, false, false, false);
			}
			
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

		player.playSound(team == StrikeTeam.T ? Radio.T_GRENADE_FLASH : Radio.CT_GRENADE_FLASH);
	}

	@Override
	public StrikeItem copy() {
		return new FlashBang();
	}
}
