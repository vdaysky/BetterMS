package obfuscate.mechanic.item.utility.grenade;

import obfuscate.mechanic.item.StrikeItem;

public class Incendiary extends FireGrenadeBase
{
	public Incendiary()
	{
		super(GrenadeType.INCENDIARY, 6000);
	}

	@Override
	public StrikeItem copy() {
		return new Incendiary();
	}
}
