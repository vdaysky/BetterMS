package obfuscate.mechanic.item.guns;

// https://bugs.mojang.com/browse/MC-146721

public enum GunType
{
	PISTOL("Pistol", 			0.0005, 	0.001, 	0.005,	3f,		0.3, 0.11, 0.5f),
	SHOTGUN("Shotgun", 		0, 		0, 		0,	3f,		0.1, 0.1, 2.5f),
	SMG("SMG", 				0.002, 	0.01, 	0.01,	3f,		0.45, 0.15, 1.2f),
	RIFLE("Rifle", 			0.004, 	0.009, 	0.04,	3.5f,	0.45, 0.5, 2f),
	SNIPER("Sniper Rifle", 	0.016, 	0.02, 	0.16,	4f,		0., 0.3, 2.5f);
	
	private final String _name;
	
	private final double _movePenalty;
	private final double _sprintPenalty;
	private final double _jumpPenalty;

	private final float _stunSeconds;
	
	private final float _volume;
	
	private final double _recoilReductionRate;
	private final double _speedPenalty; // how much to add to come per (block/second) speed


	GunType(
			String name,
			double move,
			double sprint,
			double jump,
			float volume,
			double recoilReductionRate,
			double speedPenalty,
			float stunSeconds
	)
	{
		_name = name;
		_movePenalty = move;
		_sprintPenalty = sprint;
		_jumpPenalty = jump;
		
		_volume = volume;
		
		_recoilReductionRate = recoilReductionRate;
		_speedPenalty = speedPenalty;
		_stunSeconds = stunSeconds;
	}

	public float getStunSeconds() {
		return _stunSeconds;
	}

	public String getName()
	{
		return _name;
	}
	
	public double getMovePenalty()
	{
		return _movePenalty;
	}
	
	public double getSprintPenalty()
	{
		return _sprintPenalty;
	}
	
	public double getJumpPenalty()
	{
		return _jumpPenalty;
	}

	public float getVolume()
	{
		return _volume;
	}
	
	public double getRecoilReduction()
	{
		return _recoilReductionRate;
	}

	public double getSpeedPenalty() {
		return _speedPenalty;
	}
}
