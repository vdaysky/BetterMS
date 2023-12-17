package obfuscate.game.config;

public enum ConfigField
{
    WARM_UP_DURATION("warmup-duration", 60),
    FREEZE_TIME_DURATION("freezetime-duration", 8),
    LIVE_DURATION("live-duration", 115),
    ROUND_END_DURATION("roundend-duration", 5),
    GAME_END_DURATION("gameend-duration", 30),
    BOMB_DURATION("bombplant-duration", 40),
    PAUSED_DURATION("paused-duration", -1),
    CUSTOM_DURATION("custom-duration", 10),

    INFINITE_AMMO("infiniteammo", 0, "0 - default, 1 - does not spend bullets at all, 2 - has infinite spare"),
    ALLOW_TEAM_DAMAGE("teamdamage", 0),
    START_MONEY("startmoney", 800),
    OVERTIME_MONEY("overtimemoney",10000, "amount of money each player will receive at the start of new OT"),
    FREE_GUNS("freeguns", 0),
    BUY_ANYWHERE("buyanywhere", 0),
    BUY_TIME("buytime", 40),
    ALLOW_DAMAGE("allowdamage", 1),
    WHITELIST("whitelist", 0),
    CAN_DEAD_MOVE("candeadmove", 1),
    BOMB_TICK_TIME("bombtick", 20),
    BASE_DEFUSE_TIME("defusetime", 10),
    MAX_BOMB_DAMAGE_DIST("bombrange", 60),
    BOMB_DAMAGE("bombdamage", 30),
    MAX_ROUNDS("maxrounds", 30),
    MAX_OVERTIME_ROUNDS("overtimerounds", 6),
    REQUIRE_READY("requireready", 0),
    MAX_TAC_TIMEOUTS("taccount", 3),
    TAC_DURATION("tacduration", 15),
    MAX_MONEY("maxmoney", 16000),
    KEEP_INVENTORY("keepinventory", 0, "defines whether will inventory be deleted after player death"),
    SHOP_ACCESSIBLE_BOTH_SIDES("shopaccessible", 0, "defines whether player from one team can buy other's weapons"),
    INSTANTLY_READY("instantlyready", 0, "don't touch this"), // no warm up period
    IS_RANKED("isranked", 0),
    NO_RECOIL("norecoil", 1, "whether guns follow recoil pattern"),
    NO_SPREAD("nospread", 0, "whether guns are affected by randomness"),

    WINS_BY_ELIMINATION("winsbyelimination", 1, "Whether it is possible to win by elimination"),

    RANDOM_SPAWNS("randomspawns", 0, "Whether to use Deathmatch spawn positions"),

    GRENADE_TRAJECTORY("grenadetrajectory", 0, "Whether to show grenade trajectory"),

    GRENADE_TRAJECTORY_DURATION("trajectoryduration", 20, "How long to show grenade trajectory (seconds)"),

    DEBUG_BULLET("debug_bullet_tick_trace", 0, "Whether to show bullet trajectory"),

    SHOW_IMPACTS("show_impacts", 0, "Whether to show bullet impacts"),

    SHOW_PREDICT_DIRECTION("show_predict_direction", 0, "Whether to show predicted bullet direction"),

    CAN_SEE_TEAM_NAMES("can_see_team_names", 1, "Whether to show team names above players' heads"),

    DINK_ON_KILL("dink_on_kill", 0, "Make sound on kill"),

    DEBUG_HITREG("debug_hitreg", 0, "Whether to save hitreg debug info for future use"),

    PREDICT_LOOK("predict_look", 0, "Whether to predict player's look direction"),

    PREDICT_LOOK_TICKS("predict_look_ticks", 1, "Count of ticks to predict"),

    HIGHLIGHT_BOUNDING_BOXES("highlight_bounding_boxes", 0, "Whether to highlight bounding boxes of locations"),

    SHOW_SPAWNS("show_spawns", 0, "Whether to show spawn points"),

    RETROSPECT_HITREG("retrospect_hitreg", 0, "Experimental hitreg algorithm"),

    G_MOD("gmod", 20, "Multiplier for free fall acc. Used by grenades"),

    GRENADE_DIRECT_THROW_SPEED("grenade_direct_throw_speed", 150, "Speed of direct throw"), // 170 - my lineups

    GRENADE_SHORT_THROW_SPEED("grenade_short_throw_speed", 20, "Speed of short throw"), // 20

    GRENADE_ARM_SWING_FACTOR("grenade_arm_swing_factor", 8, "Multiplier for pitch added to Y-component or throw"),

    MOVEMENT_AFFECT_GRENADE("movement_affect_grenade", 0, "Whether movement affects grenade throw"),
    GRENADE_REBOUND_VERT_PRESERVE("grenade_rebound_vert_preserve", 80, "How much of grenade speed preserved after rebound"),
    GRENADE_REBOUND_HOR_PRESERVE("grenade_rebound_hor_preserve", 30, "How much of grenade speed preserved after rebound"),

//    GRENADE_VERT_SPEED_LOSS("grenade_vert_speed_preserve", 100, ""),
//    GRENADE_HOR_SPEED_LOSS("grenade_hor_speed_preserve", 100, ""),

    GRENADE_AIR_RES("grenade_air_res", 97, "Air resistance for grenades"),

    TARGET_DISTANCE("target_distance", 20, "Distance to targets"),
    TARGET_WIDTH_SPREAD("target_width_spread", 20, "Width"),
    TARGET_HEIGHT_SPREAD("target_height_spread", 10, "Height"),
    TARGET_DISTANCE_SPREAD("target_distance_spread", 5, "Distance spread"),

    TARGET_COUNT("target_count", 10, "Number of targets"),

    TARGET_SMALL("target_small", 0, "Whether to use small targets"),

    HUMAN_TEAM("human_team", 2, "Team for humans: 0 - T, 1 - CT, 2 - NOT SET"),

    ITEM_DESTROY_ON_DROP("item_destroy_on_drop", 0, "Whether to destroy dropped items"),

    NPC_STRAFE("npc_strafe", 0, "Whether to make NPCs strafe"),

    SHOW_HITBOX("show_hitbox", 0, "Whether to show hitboxes"),

    HEAD_WIDTH("head_width", 45, "Head width"),
    HEAD_THICKNESS("head_thickness", 42, "Head thickness"),

    BODY_WIDTH("body_width", 70, "Body width"),

    BODY_THICKNESS("body_thickness", 55, "Body thickness"),

    LEG_WIDTH("leg_width", 50, "Leg width"),

    LEG_THICKNESS("leg_thickness", 45, "Leg thickness"),

    ALLOW_WALLBANG("allow_wallbang", 1, "Whether to allow wallbangs"),

    OLD_NADES("old_nades", 1, "Whether to use old nades"),

    FLASH_BASE_DURATION("flash_base_duration", 150, "Base duration of flashbang"),
    FLASH_PROXIMITY_ADDED_DURATION("flash_added_duration", 200, "Base duration of flashbang"),

    NEW_STUN("use_new_stun", 0, "Whether to use new stun"),

    NEW_STUN_MODIFIER("new_stun_modifier", 100, "Intensity of new stun effect"),

    FULL_SCOPE_DELAY("full_scope_delay", 200, "Delay in milliseconds required for snipers to get full accuracy"),

    QS_ACCURACY("qs_accuracy", 100, "Modifier that defines how much accuracy quick scope gathers before it is fully accurate. Bigger values mean more accurate shots"),

    CONE_MODIFIER("cone_modifier", 100, "Scaling for cone of fire"),

    MIN_PLAYERS("min_players", 2, "Minimum number of players required to start the game"),
    MAX_PLAYERS("max_players", 16, "Maximum number of players allowed to join the game"),

    MAX_GRENADES("max_grenades", 4, "Maximum number of grenades allowed to be bought in a single round"),

    APPLY_SPEED_PENALTY("apply_speed_penalty", 0, "Whether to apply speed penalty for guns"),

    DEBUG("debug", 0, "Whether to show debug messages"),

    CREATE_NEW_ON_COMPLETION("create_new_on_completion", 1, "Whether to create new game when this one ends"),

    USE_ICONS("use_icons", 1, "Whether to use icons for kill feed"),

    GRENADE_OG_THROW_SPEED("grenade_og_throw_speed", 145, "Speed of og throw"),
    GRENADE_OG_TOSS_SPEED("grenade_og_toss_speed", 40, "Speed of og toss"),

    USE_NMS_LAUNCH("use_nms_launch", 1, "Whether to use NMS launchProjectile impl"),

    USE_ECONOMY("use_economy", 1, "Whether to use economy"),

    ALLOW_ITEM_DROP("allow_item_drop", 1, "Whether to allow item drop"),

    BLOCK_B_SITE("block_b_site", 0, "Whether to allow bomb plant on A site only"),

    GIVE_KILL_MONEY("give_kill_money", 1, "Whether to give money for kills"),
    ;
    final int default_value;
    final String field_name;
    final String description;

    ConfigField(String field, int vDefault, String description)
    {
        field_name = field;
        default_value = vDefault;
        this.description = description;
    }

    ConfigField(String field, int vDefault)
    {
        field_name = field;
        default_value = vDefault;
        this.description = "-";
    }

    public static ConfigField getField(String fieldName)
    {

        for (ConfigField f : values())
        {

            if (f.name().equalsIgnoreCase(fieldName) || f.getName().equalsIgnoreCase(fieldName))
                return f;
        }
        return null;
    }

    public String getDescription()
    {
        return description;
    }

    public String getName()
    {
        return field_name;
    }

    public Integer getDefaultValue()
    {
        return default_value;
    }
}
