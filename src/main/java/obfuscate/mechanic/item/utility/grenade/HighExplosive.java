package obfuscate.mechanic.item.utility.grenade;

import obfuscate.game.core.Game;
import obfuscate.game.damage.DamageReason;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.sound.Radio;
import obfuscate.team.StrikeTeam;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.util.UtilParticle;
import de.slikey.effectlib.util.ParticleEffect;
import org.bukkit.Sound;

public class HighExplosive extends Grenade
{
	public HighExplosive()
	{
		super(GrenadeType.HE);
	}

	@Override
	public int getMaxStackSize()
	{
		return 1;
	}

	@Override
	public boolean updateCustom(Game game)
	{
		if (getTicksLived() > 40)
		{
			UtilParticle.PlayParticle(ParticleEffect.EXPLOSION_HUGE, getLocation(), 0, 0, 0, 0, 1);
			getLocation().getWorld().playSound(getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3f, 0.8f);
			game.getDamageManager().handleExplosion(game, _thrower, getLocation(), 14, 11, 0.85,
					this, DamageReason.GRENADE);
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

		player.playSound(team == StrikeTeam.T ? Radio.T_GRENADE_HE : Radio.CT_GRENADE_HE);
	}

	@Override
	public StrikeItem copy() {
		return new HighExplosive();
	}
}
