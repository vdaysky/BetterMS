package obfuscate.mechanic.item.guns;


import obfuscate.mechanic.item.ItemStats;
import obfuscate.mechanic.version.RecoilOffset;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;

public enum GunStats implements ItemStats
{
	//Pistols XXX
	CZ75(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "CZ75-Auto", new String[]
			{
			
			}, 
			500, 0, 100,			//Cost, Gem Cost
			12, 1, 							//Clip Size, Spare Ammo
			80, 2700, 						//ROF, Reload Time
			35, 0.006, 0.77, 	//Damage, Dropoff, Armor Penetration
			0.02, 0.05,							//COF Min, COF Max
			0.001, 										//COF Inc per Bullet
			Material.IRON_HOE, Sound.ENTITY_ENDERMAN_DEATH, false, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01),
							new RecoilOffset(0.01, 0.01)
					)
			),
			0,
			0.11 // 1.6
	),
			
	DEAGLE(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "Desert Eagle",  new String[]
			{
			
			},
			800, 0, 300,			//Cost, Gem Cost
			7, 5, 							//Clip Size, Spare Ammo
			300, 2200, 						//ROF, Reload Time
			62, 0.007, 0.80, 	//Damage, Dropoff, Armor Penetration
			0, 0.07,							//COF Min, COF Max
			0.04,										//COF Inc per Bullet
			Material.GOLDEN_HOE, Sound.ENTITY_BAT_DEATH, false, 1,
			new ArrayList<>(
					Arrays.asList(new RecoilOffset(0, 0)
					)
			),
			0,
			1.5//2.3
	),
				
	GLOCK_18(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "Glock 18", new String[]
			{
			
			}, 
			200, 0, 300,			//Cost, Gem Cost
			20, 6, 							//Clip Size, Spare Ammo
			120, 2200, 						//ROF, Reload Time
			// 25 would be 1 tap hs if close with no armor
			30, 0.01, 0.47, 	//Damage, Dropoff, Armor Penetration
			0.01, 0.03,						//COF Min, COF Max
			0.0003, 										//COF Inc per Bullet
			Material.STONE_HOE, Sound.ENTITY_BAT_LOOP, false, 1,
			new ArrayList<>(
					Arrays.asList(new RecoilOffset(0, 0)
					)
			),
			0,
			1.1//1
	),
			
	P2000(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "USP-S",  new String[]
			{
			
			},
			200, 0, 300,			//Cost, Gem Cost
			13, 4, 							//Clip Size, Spare Ammo
			130, 2200, 						//ROF, Reload Time
			32, 0.008, 0.50, 	//Damage, Dropoff, Armor Penetration
			0.005, 0.03,						//COF Min, COF Max
			0.0003, 										//COF Inc per Bullet
			Material.WOODEN_HOE, Sound.ENTITY_GHAST_SCREAM, false, 1,
			new ArrayList<>(
					Arrays.asList(new RecoilOffset(0, 0)
					)
			),
			0,
			1.3 //1.6
	),
	
	P250(StrikeItemType.SECONDARY_WEAPON, GunType.PISTOL, "P250",  new String[]
			{
			
			},
			300, 0, 300,			//Cost, Gem Cost
			13, 4, 							//Clip Size, Spare Ammo
			130, 2200, 						//ROF, Reload Time
			32, 0.005, 0.77, 	//Damage, Dropoff, Armor Penetration
			0, 0.03,							//COF Min, COF Max
			0.0003,										//COF Inc per Bullet
			Material.DIAMOND_HOE, Sound.ENTITY_SILVERFISH_DEATH, false, 1,
			new ArrayList<>(
					Arrays.asList(new RecoilOffset(0, 0)
					)
			),
			0,
			1.5 //1.8
	),
	
	
	//Rifles XXX
	FAMAS(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "FAMAS",  new String[]
			{
			
			},
			2250, 5000, 300,		//Cost, Gem Cost
			25, 4, 							//Clip Size, Spare Ammo
			80, 3300, 						//ROF, Reload Time
			30, 0.004, 0.7, 	//Damage, Dropoff, Armor Penetration
			0.012, 0.09,							//COF Min, COF Max
			0.02, 										//COF Inc per Bullet
			Material.WOODEN_PICKAXE, Sound.ENTITY_WITHER_DEATH, false, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0, 0),
							new RecoilOffset(0.003444676409185804, 0.004879958246346556),
							new RecoilOffset(0.0020093945720250526, 0.008898747390396661),
							new RecoilOffset(0.00861169102296451, 0.019232776617954073),
							new RecoilOffset(0.010046972860125262, 0.035594989561586646),
							new RecoilOffset(0.009759916492693112, 0.057698329853862224),
							new RecoilOffset(-0.004592901878914406, 0.07578288100208769),
							new RecoilOffset(-0.020668058455114824, 0.087839248434238),
							new RecoilOffset(-0.013204592901878916, 0.10046972860125263),
							new RecoilOffset(0.00775052192066806, 0.10793319415448853),
							new RecoilOffset(0.022103340292275578, 0.11310020876826724),
							new RecoilOffset(0.03616910229645094, 0.11482254697286014),
							new RecoilOffset(0.03128914405010439, 0.1208507306889353),
							new RecoilOffset(0.007176409185803759, 0.1248695198329854),
							new RecoilOffset(-0.005167014613778706, 0.13032359081419626),
							new RecoilOffset(-0.024112734864300628, 0.1277400835073069),
							new RecoilOffset(-0.029853862212943637, 0.12831419624217122),
							new RecoilOffset(-0.04592901878914406, 0.1277400835073069),
							new RecoilOffset(-0.048225469728601256, 0.13233298538622132),
							new RecoilOffset(-0.043919624217119, 0.13577766179540712),
							new RecoilOffset(-0.027270354906054284, 0.13635177453027142),
							new RecoilOffset(-0.01636221294363257, 0.1375),
							new RecoilOffset(-0.019232776617954073, 0.1346294363256785),
							new RecoilOffset(-0.04133611691022965, 0.12974947807933196),
							new RecoilOffset(-0.057698329853862224, 0.11855427974947809)
					)
			),
			0,
			1.9 //2
	),
	
	GALIL(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "Galil AR",  new String[]
			{
			
			},
			2000, 5000, 300, 		//Cost, Gem Cost
			30, 3, 								//Clip Size, Spare Ammo
			80, 2600, 							//ROF, Reload Time
			30, 0.004, 0.75, 		//Damage, Dropoff, Armor Penetration
			0.009, 0.09,								//COF Min, COF Max
			0.02, 											//COF Inc per Bullet
			Material.STONE_PICKAXE, Sound.ENTITY_WITHER_SHOOT, false, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(-0.000275, 0),
							new RecoilOffset(-0.00385, 0.00385),
							new RecoilOffset(-0.0011, 0.010725),
							new RecoilOffset(-0.007975000000000001, 0.0198),
							new RecoilOffset(-0.018975000000000002, 0.032175),
							new RecoilOffset(-0.017875000000000002, 0.0539),
							new RecoilOffset(-0.018975000000000002, 0.076725),
							new RecoilOffset(-0.02695, 0.0946),
							new RecoilOffset(-0.0374, 0.10175000000000001),
							new RecoilOffset(-0.03245, 0.11632500000000001),
							new RecoilOffset(-0.011000000000000001, 0.1232),
							new RecoilOffset(0.017875000000000002, 0.12127500000000001),
							new RecoilOffset(0.046200000000000005, 0.10945),
							new RecoilOffset(0.055, 0.115775),
							new RecoilOffset(0.06655, 0.11825000000000001),
							new RecoilOffset(0.071775, 0.11825000000000001),
							new RecoilOffset(0.07287500000000001, 0.12155),
							new RecoilOffset(0.069575, 0.12732500000000002),
							new RecoilOffset(0.044000000000000004, 0.13392500000000002),
							new RecoilOffset(0.029975, 0.1375),
							new RecoilOffset(0.006325000000000001, 0.13392500000000002),
							new RecoilOffset(-0.022825, 0.1265),
							new RecoilOffset(-0.028325000000000003, 0.12760000000000002),
							new RecoilOffset(-0.030250000000000003, 0.13090000000000002),
							new RecoilOffset(-0.0176, 0.131725),
							new RecoilOffset(-0.040425, 0.128975),
							new RecoilOffset(-0.046475, 0.130075),
							new RecoilOffset(-0.055275000000000005, 0.126225),
							new RecoilOffset(-0.0154, 0.12512500000000001),
							new RecoilOffset(0.009075, 0.12265000000000001),
							new RecoilOffset(0.021175000000000003, 0.12815000000000001),
							new RecoilOffset(0.0165, 0.136125),
							new RecoilOffset(0.029975, 0.13255),
							new RecoilOffset(0.05335, 0.119075),
							new RecoilOffset(0.06682500000000001, 0.11770000000000001)
					)
			),
			0,
			1.8 // 2
	),
			
			
	AK47(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "AK-47",  new String[]
			{
			
			},
			2700, 5000,300, 		//Cost, Gem Cost
			30, 3, 							//Clip Size, Spare Ammo
			80, 2500, 						//ROF, Reload Time
			36, 0.004, 0.78, 	//Damage, Dropoff, Armor Penetration
			0, 0.09/*0.15*/,							//COF Min, COF Max
			0.017,										//COF Inc per Bullet
			Material.WOODEN_SHOVEL, Sound.ENTITY_PLAYER_BURP, false, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(-0.0001697530864197531, 0),
							new RecoilOffset(0.0020370370370370373, 0.00492283950617284),
							new RecoilOffset(-0.0001697530864197531, 0.01646604938271605),
							new RecoilOffset(0.0011882716049382716, 0.03513888888888889),
							new RecoilOffset(0.0020370370370370373, 0.05449074074074075),
							new RecoilOffset(-0.006111111111111112, 0.07570987654320989),
							new RecoilOffset(-0.010864197530864199, 0.09285493827160494),
							new RecoilOffset(-0.019351851851851853, 0.10541666666666669),
							new RecoilOffset(-0.008657407407407409, 0.11424382716049385),
							new RecoilOffset(0.018163580246913582, 0.1122067901234568),
							new RecoilOffset(0.032253086419753094, 0.11339506172839507),
							new RecoilOffset(0.02359567901234568, 0.1208641975308642),
							new RecoilOffset(0.03395061728395062, 0.12442901234567903),
							new RecoilOffset(0.05058641975308643, 0.11882716049382717),
							new RecoilOffset(0.052793209876543214, 0.12222222222222223),
							new RecoilOffset(0.027160493827160497, 0.1234104938271605),
							new RecoilOffset(0.014259259259259261, 0.1278240740740741),
							new RecoilOffset(0.005601851851851853, 0.13444444444444445),
							new RecoilOffset(-0.011882716049382718, 0.1335956790123457),
							new RecoilOffset(-0.03276234567901235, 0.12731481481481483),
							new RecoilOffset(-0.020030864197530866, 0.12612654320987657),
							new RecoilOffset(-0.02359567901234568, 0.1278240740740741),
							new RecoilOffset(-0.02020061728395062, 0.13342592592592595),
							new RecoilOffset(-0.014089506172839508, 0.1358024691358025),
							new RecoilOffset(-0.026311728395061733, 0.1346141975308642),
							new RecoilOffset(-0.030725308641975313, 0.1375),
							new RecoilOffset(-0.01697530864197531, 0.1371604938271605),
							new RecoilOffset(0.0035648148148148154, 0.13546296296296298),
							new RecoilOffset(0.0329320987654321, 0.12205246913580249),
							new RecoilOffset(0.041929012345679015, 0.12171296296296298)
					)
			),
			0,
			2.1 // 2.5
	),
	
	M4A4(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "M4A4",  new String[]
			{
			
			},
			2900, 5000, 300,		//Cost, Gem Cost
			30, 3, 							//Clip Size, Spare Ammo
			80, 3000, 						//ROF, Reload Time
			33, 0.004, 0.7, 	//Damage, Dropoff, Armor Penetration
			0, 0.09,							//COF Min, COF Max
			0.015, 										//COF Inc per Bullet
			Material.STONE_SHOVEL, Sound.ENTITY_BAT_TAKEOFF, false, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0, 0),
							new RecoilOffset(0.0008025291828793775, 0.0021400778210116734),
							new RecoilOffset(-0.0008025291828793775, 0.010700389105058366),
							new RecoilOffset(0.0037451361867704286, 0.02514591439688716),
							new RecoilOffset(-0.0008025291828793775, 0.0438715953307393),
							new RecoilOffset(0.005082684824902724, 0.06099221789883269),
							new RecoilOffset(0.00989785992217899, 0.08052042801556421),
							new RecoilOffset(-0.004012645914396888, 0.09202334630350195),
							new RecoilOffset(-0.011502918287937744, 0.10406128404669261),
							new RecoilOffset(-0.02915856031128405, 0.10834143968871596),
							new RecoilOffset(-0.024343385214007786, 0.11288910505836577),
							new RecoilOffset(-0.010967898832684826, 0.12037937743190663),
							new RecoilOffset(0.011235408560311286, 0.11716926070038912),
							new RecoilOffset(0.02996108949416343, 0.11476167315175098),
							new RecoilOffset(0.05029182879377432, 0.10994649805447472),
							new RecoilOffset(0.050826848249027244, 0.11422665369649806),
							new RecoilOffset(0.0642023346303502, 0.11449416342412452),
							new RecoilOffset(0.06152723735408561, 0.11957684824902724),
							new RecoilOffset(0.0510943579766537, 0.12037937743190663),
							new RecoilOffset(0.0452091439688716, 0.11984435797665371),
							new RecoilOffset(0.033973735408560315, 0.12011186770428017),
							new RecoilOffset(0.026750972762645917, 0.12412451361867706),
							new RecoilOffset(0.006687743190661479, 0.12412451361867706),
							new RecoilOffset(-0.0013375486381322957, 0.12599708171206228),
							new RecoilOffset(-0.001605058365758755, 0.13241731517509728),
							new RecoilOffset(-0.006955252918287938, 0.13348735408560314),
							new RecoilOffset(-0.009362840466926071, 0.13054474708171207),
							new RecoilOffset(-0.010700389105058366, 0.1356274319066148),
							new RecoilOffset(-0.015515564202334632, 0.1375),
							new RecoilOffset(-0.015515564202334632, 0.1375),
							new RecoilOffset(-0.015515564202334632, 0.1375)

							)
			),
			0,
			1.9 //2.3
	),
	
	SG553(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "SG553",  new String[]
			{
			
			},
			3000, 5000,300, 		//Cost, Gem Cost
			30, 3, 							//Clip Size, Spare Ammo
			80, 3800, 						//ROF, Reload Time
			30, 0.004, 1.00, 	//Damage, Dropoff, Armor Penetration
			0, 0.09,							//COF Min, COF Max
			0.01, 										//COF Inc per Bullet
			Material.IRON_PICKAXE, Sound.ENTITY_WITHER_SPAWN, true, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(-0.0006926952141057935, 0),
							new RecoilOffset(0.0020780856423173806, 0.005541561712846348),
							new RecoilOffset(0.010044080604534007, 0.015239294710327457),
							new RecoilOffset(0.014892947103274561, 0.030824937027707812),
							new RecoilOffset(0.018702770780856425, 0.04848866498740555),
							new RecoilOffset(0.023205289672544083, 0.06857682619647357),
							new RecoilOffset(0.028400503778337534, 0.08970403022670026),
							new RecoilOffset(0.040176322418136026, 0.09905541561712848),
							new RecoilOffset(0.03117128463476071, 0.11048488664987408),
							new RecoilOffset(0.036020151133501266, 0.11706549118387911),
							new RecoilOffset(0.046756926952141065, 0.12087531486146097),
							new RecoilOffset(0.05022040302267003, 0.12537783375314862),
							new RecoilOffset(0.04537153652392948, 0.1284949622166247),
							new RecoilOffset(0.05160579345088162, 0.13126574307304786),
							new RecoilOffset(0.04918136020151134, 0.1375),
							new RecoilOffset(0.05818639798488666, 0.13507556675062973),
							new RecoilOffset(0.07065491183879094, 0.12364609571788415),
							new RecoilOffset(0.08069899244332494, 0.1187972292191436),
							new RecoilOffset(0.0855478589420655, 0.11706549118387911),
							new RecoilOffset(0.061649874055415624, 0.11845088161209069),
							new RecoilOffset(0.02666876574307305, 0.11498740554156173),
							new RecoilOffset(0, 0.11464105793450884),
							new RecoilOffset(-0.0107367758186398, 0.12018261964735517),
							new RecoilOffset(-0.019741813602015115, 0.12710957178841312),
							new RecoilOffset(-0.02355163727959698, 0.130919395465995),
							new RecoilOffset(-0.03775188916876575, 0.12884130982367759),
							new RecoilOffset(-0.052991183879093205, 0.1267632241813602),
							new RecoilOffset(-0.050913098236775825, 0.13126574307304786),
							new RecoilOffset(-0.04156171284634761, 0.13438287153652395)
					)
			),
			0,
			2.1 // 2.5
	),
			
	AUG(StrikeItemType.PRIMARY_WEAPON, GunType.RIFLE, "Steyr AUG",  new String[]
			{
			
			},
			3300, 5000,300, 		//Cost, Gem Cost
			30, 3, 							//Clip Size, Spare Ammo
			80, 3800, 						//ROF, Reload Time
			28, 0.004, 0.9, 	//Damage, Dropoff, Armor Penetration
			0, 0.1,							//COF Min, COF Max
			0.012,										//COF Inc per Bullet
			Material.GOLDEN_PICKAXE, Sound.ENTITY_VILLAGER_DEATH, true, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0.0004493464052287582, 0),
							new RecoilOffset(-0.002920751633986928, 0.004493464052287582),
							new RecoilOffset(-0.002696078431372549, 0.01280637254901961),
							new RecoilOffset(0.0006740196078431372, 0.028308823529411768),
							new RecoilOffset(0.00516748366013072, 0.046058006535947715),
							new RecoilOffset(0.0015727124183006536, 0.06515522875816994),
							new RecoilOffset(-0.004268790849673203, 0.08604983660130719),
							new RecoilOffset(-0.013480392156862746, 0.10020424836601308),
							new RecoilOffset(-0.018423202614379085, 0.11098856209150328),
							new RecoilOffset(-0.026511437908496735, 0.11862745098039217),
							new RecoilOffset(-0.015951797385620917, 0.12671568627450983),
							new RecoilOffset(-0.01213235294117647, 0.13075980392156863),
							new RecoilOffset(-0.021568627450980392, 0.13053513071895426),
							new RecoilOffset(-0.02269199346405229, 0.1354779411764706),
							new RecoilOffset(-0.006740196078431373, 0.1375),
							new RecoilOffset(0.01752450980392157, 0.1300857843137255),
							new RecoilOffset(0.03999183006535948, 0.12199754901960785),
							new RecoilOffset(0.04246323529411765, 0.12604166666666666),
							new RecoilOffset(0.0451593137254902, 0.12941176470588237),
							new RecoilOffset(0.05100081699346406, 0.1289624183006536),
							new RecoilOffset(0.03370098039215687, 0.12963643790849674),
							new RecoilOffset(0.01258169934640523, 0.13255718954248366),
							new RecoilOffset(0.005841503267973856, 0.13682598039215688),
							new RecoilOffset(0.0024714052287581703, 0.13615196078431374),
							new RecoilOffset(-0.004493464052287582, 0.13525326797385623),
							new RecoilOffset(-0.024714052287581702, 0.1280637254901961),
							new RecoilOffset(-0.03774509803921569, 0.12828839869281047),
							new RecoilOffset(-0.02718545751633987, 0.130984477124183),
							new RecoilOffset(-0.016176470588235296, 0.1332312091503268),
							new RecoilOffset(-0.013929738562091504, 0.13390522875816993)
							)
			),
			0,
			2.0//2.5
	),
	
	//Sniper XXX
	AWP(StrikeItemType.PRIMARY_WEAPON, GunType.SNIPER, "AWP",  new String[]
			{
			
			},
			4750, 5000, 100,			//Cost, Gem Cost
			10, 3, 								//Clip Size, Spare Ammo
			1500, 3600, 							//ROF, Reload Time
			115, 0, 0.97, 			//Damage, Dropoff, Armor Penetration
			0.09, 0.09,//0.2, 0.2,					//COF Min, COF Max
			0, 												//COF Inc per Bullet
			Material.GOLDEN_SHOVEL, Sound.ENTITY_GENERIC_DRINK, true, 1,
			new ArrayList<>(
					Arrays.asList(new RecoilOffset(0, 0)
					)
			),
			0,
			2.2//5
	),
			
	SSG08(StrikeItemType.PRIMARY_WEAPON, GunType.SNIPER, "SSG 08",  new String[]
			{
			
			},
			1700, 5000, 300,			//Cost, Gem Cost
			10, 6, 								//Clip Size, Spare Ammo
			1250, 3700, 							//ROF, Reload Time
			88, 0.001, 0.85, 		//Damage, Dropoff, Armor Penetration
			0.065, 0.065,							//COF Min, COF Max
			0, 												//COF Inc per Bullet
			Material.DIAMOND_PICKAXE, Sound.ENTITY_WOLF_DEATH, true, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0, 0)
					)
			),
			0,
			1.8//3
	),
	
	//Shotgun XXX
	NOVA(StrikeItemType.PRIMARY_WEAPON, GunType.SHOTGUN, "Nova",  new String[]
			{

			},
			1200, 5000, 900,		//Cost, Gem Cost
			8, 4, 							//Clip Size, Spare Ammo
			1000, 600, 						//ROF, Reload Time
			20, 0.04, 0.5, 		//Damage, Dropoff, Armor Penetration
			0.06, 0.06,						//COF Min, COF Max
			0, 											//COF Inc per Bullet
			Material.GOLDEN_AXE, Sound.ENTITY_BLAZE_DEATH, false, 9,
			new ArrayList<>(
				Arrays.asList(
						new RecoilOffset(0, 0)
				)
			),
			0,
			1.5//3
	),

	XM1014(StrikeItemType.PRIMARY_WEAPON, GunType.SHOTGUN, "XM1014",  new String[]
			{

			},
			2000, 5000, 900,		//Cost, Gem Cost
			7, 4, 							//Clip Size, Spare Ammo
			260, 600, 						//ROF, Reload Time
			16, 0.04, 0.7, 		//Damage, Dropoff, Armor Penetration
			0.07, 0.07,						//COF Min, COF Max
			0, 											//COF Inc per Bullet
			Material.DIAMOND_AXE, Sound.ENTITY_SKELETON_DEATH, false, 6,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0, 0)
					)
			),
			0,
			1.3//3
	),
	
	//Smg XXX
	P90(StrikeItemType.PRIMARY_WEAPON, GunType.SMG, "P90", new String[]
			{
			
			},
			2350, 5000, 300, 		//Cost, Gem Cost
			50, 2, 								//Clip Size, Spare Ammo
			35, 3300, 							//ROF, Reload Time
			17, 0.006, 0.65, 		//Damage, Dropoff, Armor Penetration
			0.025, 0.09,							//COF Min, COF Max
			0.009, 											//COF Inc per Bullet
			Material.STONE_AXE, Sound.ENTITY_CREEPER_DEATH, false, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0.0005813953488372094, 0),
							new RecoilOffset(0.0031976744186046516, 0.0031976744186046516),
							new RecoilOffset(0.002906976744186047, 0.007848837209302326),
							new RecoilOffset(0.0011627906976744188, 0.012500000000000002),
							new RecoilOffset(0.0037790697674418613, 0.023255813953488375),
							new RecoilOffset(0.014534883720930236, 0.03779069767441861),
							new RecoilOffset(0.026162790697674423, 0.049709302325581406),
							new RecoilOffset(0.03633720930232559, 0.06715116279069769),
							new RecoilOffset(0.027325581395348842, 0.08023255813953489),
							new RecoilOffset(0.01802325581395349, 0.09186046511627909),
							new RecoilOffset(0.015697674418604653, 0.10377906976744188),
							new RecoilOffset(0.013372093023255816, 0.11395348837209304),
							new RecoilOffset(0.016279069767441864, 0.12122093023255816),
							new RecoilOffset(0.027616279069767446, 0.12151162790697677),
							new RecoilOffset(0.03401162790697675, 0.12500000000000003),
							new RecoilOffset(0.03779069767441861, 0.1293604651162791),
							new RecoilOffset(0.04040697674418605, 0.12703488372093025),
							new RecoilOffset(0.05000000000000001, 0.12732558139534886),
							new RecoilOffset(0.023546511627906982, 0.1299418604651163),
							new RecoilOffset(0.012500000000000002, 0.13168604651162794),
							new RecoilOffset(0.0031976744186046516, 0.13255813953488374),
							new RecoilOffset(0.0002906976744186047, 0.1290697674418605),
							new RecoilOffset(-0.010174418604651165, 0.13255813953488374),
							new RecoilOffset(-0.013372093023255816, 0.1299418604651163),
							new RecoilOffset(-0.01976744186046512, 0.12790697674418608),
							new RecoilOffset(-0.0247093023255814, 0.12790697674418608),
							new RecoilOffset(0, 0.1290697674418605),
							new RecoilOffset(0.017732558139534886, 0.13168604651162794),
							new RecoilOffset(0.02238372093023256, 0.13401162790697677),
							new RecoilOffset(0.026453488372093027, 0.13430232558139538),
							new RecoilOffset(0.023546511627906982, 0.1366279069767442),
							new RecoilOffset(0.01976744186046512, 0.13720930232558143),
							new RecoilOffset(0.015116279069767445, 0.13720930232558143),
							new RecoilOffset(0.011627906976744188, 0.1363372093023256),
							new RecoilOffset(0.006395348837209303, 0.13720930232558143),
							new RecoilOffset(0.0037790697674418613, 0.1375),
							new RecoilOffset(0.0011627906976744188, 0.1375),
							new RecoilOffset(-0.0034883720930232566, 0.136046511627907),
							new RecoilOffset(0.006395348837209303, 0.13720930232558143),
							new RecoilOffset(0.012500000000000002, 0.13720930232558143),
							new RecoilOffset(0.02412790697674419, 0.13691860465116282),
							new RecoilOffset(-0.005523255813953489, 0.13168604651162794),
							new RecoilOffset(-0.016279069767441864, 0.13139534883720932),
							new RecoilOffset(-0.027616279069767446, 0.1299418604651163),
							new RecoilOffset(-0.04127906976744187, 0.12761627906976747),
							new RecoilOffset(-0.04622093023255815, 0.1302325581395349),
							new RecoilOffset(-0.05087209302325582, 0.12703488372093025),
							new RecoilOffset(-0.05668604651162792, 0.12674418604651164),
							new RecoilOffset(-0.060755813953488384, 0.12470930232558142),
							new RecoilOffset(-0.04447674418604652, 0.1299418604651163)
					)
			),
			0,
			0.9//1.1
	),
	
	PPBIZON(StrikeItemType.PRIMARY_WEAPON, GunType.SMG, "PP-Bizon", new String[]
			{
			
			},
			1400, 5000,600,		//Cost, Gem Cost
			64, 2, 							//Clip Size, Spare Ammo
			60, 2400, 						//ROF, Reload Time
			27, 0.007, 0.47, 	//Damage, Dropoff, Armor Penetration
			0.025, 0.1,						//COF Min, COF Max
			0.01,										//COF Inc per Bullet
			Material.WOODEN_AXE, Sound.ENTITY_SHEEP_SHEAR, false, 1,
			new ArrayList<>(
					Arrays.asList(
							new RecoilOffset(0.00036962365591397853, 0),
							new RecoilOffset(0.0018481182795698926, 0.004435483870967743),
							new RecoilOffset(0.005544354838709678, 0.008501344086021507),
							new RecoilOffset(0.007762096774193549, 0.015524193548387098),
							new RecoilOffset(0.01515456989247312, 0.02735215053763441),
							new RecoilOffset(0.02772177419354839, 0.04176747311827957),
							new RecoilOffset(0.024764784946236563, 0.06098790322580646),
							new RecoilOffset(0.015524193548387098, 0.07946908602150539),
							new RecoilOffset(0.014415322580645163, 0.09721102150537636),
							new RecoilOffset(-0.0033266129032258067, 0.10164650537634409),
							new RecoilOffset(-0.017372311827956993, 0.10719086021505378),
							new RecoilOffset(-0.02254704301075269, 0.11532258064516131),
							new RecoilOffset(-0.03585349462365592, 0.11680107526881722),
							new RecoilOffset(-0.02956989247311828, 0.12493279569892475),
							new RecoilOffset(-0.02291666666666667, 0.12641129032258067),
							new RecoilOffset(-0.017372311827956993, 0.12678091397849464),
							new RecoilOffset(-0.005544354838709678, 0.12382392473118281),
							new RecoilOffset(-0.004435483870967743, 0.12715053763440862),
							new RecoilOffset(-0.018850806451612905, 0.1308467741935484),
							new RecoilOffset(-0.025873655913978496, 0.13232526881720433),
							new RecoilOffset(-0.036592741935483876, 0.13121639784946237),
							new RecoilOffset(-0.05433467741935485, 0.12899865591397852),
							new RecoilOffset(-0.06061827956989248, 0.12788978494623657),
							new RecoilOffset(-0.06801075268817205, 0.12862903225806452),
							new RecoilOffset(-0.07429435483870969, 0.12641129032258067),
							new RecoilOffset(-0.07466397849462367, 0.12123655913978496),
							new RecoilOffset(-0.06948924731182797, 0.12271505376344087),
							new RecoilOffset(-0.0654233870967742, 0.12456317204301076),
							new RecoilOffset(-0.06505376344086022, 0.12862903225806452),
							new RecoilOffset(-0.044724462365591404, 0.13158602150537635),
							new RecoilOffset(-0.049529569892473126, 0.13528225806451613),
							new RecoilOffset(-0.0413978494623656, 0.13528225806451613),
							new RecoilOffset(-0.03991935483870968, 0.1375),
							new RecoilOffset(-0.034375, 0.13713037634408604),
							new RecoilOffset(-0.026243279569892475, 0.1360215053763441),
							new RecoilOffset(-0.02217741935483871, 0.1341733870967742),
							new RecoilOffset(-0.02772177419354839, 0.1293682795698925),
							new RecoilOffset(-0.03289650537634409, 0.12899865591397852),
							new RecoilOffset(-0.03991935483870968, 0.12973790322580647),
							new RecoilOffset(-0.041028225806451615, 0.12678091397849464),
							new RecoilOffset(-0.04879032258064517, 0.12788978494623657),
							new RecoilOffset(-0.05248655913978495, 0.12456317204301076),
							new RecoilOffset(-0.05285618279569893, 0.12197580645161292),
							new RecoilOffset(-0.055073924731182805, 0.12271505376344087),
							new RecoilOffset(-0.06172715053763442, 0.12382392473118281),
							new RecoilOffset(-0.06394489247311828, 0.12197580645161292),
							new RecoilOffset(-0.062466397849462375, 0.11827956989247312),
							new RecoilOffset(-0.06616263440860216, 0.11606182795698926),
							new RecoilOffset(-0.06875, 0.11864919354838711),
							new RecoilOffset(-0.07244623655913979, 0.11458333333333334),
							new RecoilOffset(-0.07096774193548389, 0.11236559139784948),
							new RecoilOffset(-0.0757728494623656, 0.11790994623655915),
							new RecoilOffset(-0.07836021505376345, 0.11754032258064517),
							new RecoilOffset(-0.08575268817204303, 0.11458333333333334),
							new RecoilOffset(-0.06911962365591398, 0.12160618279569894),
							new RecoilOffset(-0.06098790322580646, 0.12345430107526884),
							new RecoilOffset(-0.05063844086021506, 0.1253024193548387),
							new RecoilOffset(-0.04250672043010753, 0.12862903225806452),
							new RecoilOffset(-0.030678763440860218, 0.12862903225806452),
							new RecoilOffset(-0.02032930107526882, 0.1308467741935484),
							new RecoilOffset(-0.017002688172043014, 0.13158602150537635),
							new RecoilOffset(-0.0103494623655914, 0.12715053763440862),
							new RecoilOffset(0, 0.12678091397849464),
							new RecoilOffset(0.007762096774193549, 0.1253024193548387),
							new RecoilOffset(0.011827956989247313, 0.12456317204301076),
							new RecoilOffset(0.004805107526881721, 0.1326948924731183)
					)
			),
			0.1,
			0.9//1.1
	);
	
	
	private GunType _gunType;
	private StrikeItemType _itemType;
	
	private String _name;
	private String[] _desc;
	
	private int _cost;
	private int _gemCost;
	
	private int _clipSize;
	private int _clipReserve;
	private long _fireRate;
	private long _reloadTime;
	private double _damage;
	private double _dropOffPerBlock;
	private double _armorPen;
	
	private Material _skin;
	private Sound _fireSound;

	private double _coneMin;
	private double _coneMax;
	private double _coneReduceRate;
	private double _coneIncreaseRate;
	
	private boolean _scope = false;
	private int _pellets;
	private int _killReward;

	private ArrayList<RecoilOffset> _sprayPattern;
	private double _sprayInaccuracy;
	private double _maxWallbangDist;
	
	GunStats(StrikeItemType type, GunType gunType, String name, String[] desc,
             int cost, int gemCost, int killReward,
             int clipSize, int clipReserve,
             long fireRate, long reloadTime,
             double damage, double dropOffPerBlock, double armorPen,
             double coneMin, double coneMax, double coneIncrease,
             Material skin, Sound sound, boolean scope, int pellets,
			 ArrayList<RecoilOffset> sprayPattern, double sprayInaccuracy, double maxWallbangDist)
	{
		_itemType = type;
		_gunType = gunType;

		_name = name;
		_desc = desc;
		
		_cost = cost;
		_gemCost = gemCost;
		_killReward = killReward;

		_clipSize = clipSize;
		_clipReserve = clipReserve;
		_fireRate = fireRate;
		_reloadTime = reloadTime;
		_damage = damage;
		_dropOffPerBlock = dropOffPerBlock;
		_armorPen = armorPen;

		_skin = skin;
		_fireSound = sound;

		_coneMin = coneMin;
		_coneMax = coneMax;
		_coneIncreaseRate = coneIncrease;
		
		_scope = scope;
		_pellets = pellets;
		_sprayPattern = sprayPattern;
		_sprayInaccuracy = sprayInaccuracy;
		_maxWallbangDist = maxWallbangDist;
	}
	public int getKillReward(){
		return _killReward;
	}

	public StrikeItemType getItemType()
	{
		return _itemType;
	}

	public GunType getGunType()
	{
		return _gunType;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String[] getDesc()
	{
		return _desc;
	}
	
	public int getCost()
	{
		return _cost;
	}
	
	public int getGemCost()
	{
		return _gemCost;
	}
	
	public int getClipSize()
	{
		return _clipSize;
	}
	
	public int getClipReserve()
	{
		return _clipReserve;
	}
	
	public long getFireRate()
	{
		return _fireRate;
	}
	
	public long getReloadTime()
	{
		return _reloadTime;
	}
	
	public double getDamage()
	{
		return _damage;
	}
	
	public double getDropoff()
	{
		return _dropOffPerBlock;
	}
	
	public double getArmorPen()
	{
		return _armorPen;
	}
	
	public Material getSkin()
	{
		return _skin;
	}
	
	public Sound getFireSound()
	{
		return _fireSound;
	}
	
	public double getConeMin()
	{
		return _coneMin;
	}
	
	public double getConeMax()
	{
		return _coneMax;
	}
	
	public double getConeReduceRate()
	{
		return _gunType.getRecoilReduction();
	}
	
	public double getConeIncreaseRate()
	{
		return _coneIncreaseRate;
	}
	
	public boolean getScope()
	{
		return _scope;
	}
	
	public int getPellets()
	{
		return _pellets;
	}

	public ArrayList<RecoilOffset> getSprayPattern() {
		return _sprayPattern;
	}

	public double getSprayInaccuracy() {
		return _sprayInaccuracy;
	}

	/** value ranged from 0 to 1, representing how much of original damage will remain after 1-block wallbang */
	public double getMaxWallbangDist() {
		return _maxWallbangDist;
	}

	public boolean isSniper() {
		return _gunType == GunType.SNIPER;
	}
}
