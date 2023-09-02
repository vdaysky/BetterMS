package obfuscate.mechanic.item.utility.grenade;

import obfuscate.mechanic.item.StrikeItem;

public class Molotov extends FireGrenadeBase
{
	public Molotov()
	{
		super(GrenadeType.MOLOTOV, 6000);
	}

	@Override
	public StrikeItem copy() {
		return new Molotov();
	}
}
