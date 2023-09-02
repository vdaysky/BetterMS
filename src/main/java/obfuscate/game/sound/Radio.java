package obfuscate.game.sound;

import org.bukkit.Sound;

import java.util.Random;

public enum Radio
{
    BOMB_PLANT(new Sound[] {Sound.ENTITY_WOLF_PANT}),
    BOMB_DEFUSE(new Sound[] {Sound.ENTITY_WOLF_SHAKE}),
    CT_WIN(new Sound[] {Sound.ENTITY_WOLF_WHINE}),
    T_WIN(new Sound[] {Sound.ENTITY_ZOMBIE_DEATH}),

    CT_GRENADE_HE(new Sound[] {Sound.ENTITY_SPIDER_AMBIENT}),
    CT_GRENADE_FLASH(new Sound[] {Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR}),
    CT_GRENADE_SMOKE(new Sound[] {Sound.ENTITY_WOLF_GROWL}),
    CT_GRENADE_FIRE(new Sound[] {Sound.ENTITY_WOLF_HOWL}),

    T_GRENADE_HE(new Sound[] {Sound.ENTITY_WITHER_HURT}),
    T_GRENADE_FLASH(new Sound[] {Sound.ENTITY_WOLF_AMBIENT}),
    T_GRENADE_SMOKE(new Sound[] {Sound.ENTITY_VILLAGER_AMBIENT}),
    T_GRENADE_FIRE(new Sound[] {Sound.ENTITY_WITHER_AMBIENT}),

    CT_START(new Sound[] {Sound.ENTITY_VILLAGER_HURT}),
    T_START(new Sound[] {Sound.ENTITY_VILLAGER_TRADE}),

    T_BOMB_PLANT(new Sound[] {Sound.ENTITY_ZOMBIE_VILLAGER_CURE}),
    T_BOMB_DROP(new Sound[] {Sound.ENTITY_ZOMBIE_INFECT}),

    // Gun Game
    GG_BRASS_BELL(new Sound[] {}),
    GG_KNIFE_LEVEL_UP(new Sound[] {Sound.ENTITY_COW_DEATH}),

    GG_LEVEL_UP(new Sound[] {Sound.ENTITY_COW_HURT}),

    GG_LOST_LEAD(new Sound[] {Sound.ENTITY_COW_MILK}),

    GG_GRENADE_LEVEL_UP(new Sound[] {Sound.ENTITY_COW_STEP}),

    GG_TAKEN_LEAD(new Sound[] {Sound.BLOCK_BEACON_ACTIVATE}),

    GG_TIED_LEAD(new Sound[] {Sound.BLOCK_BEACON_AMBIENT}),

    GG_TRIPLE_KILL(new Sound[] {Sound.BLOCK_BEACON_DEACTIVATE}),

    GG_WELCOME(new Sound[] {Sound.BLOCK_BEACON_POWER_SELECT}),
    ;

    private final Sound[] _sounds;
    private final Random r = new Random();

    Radio(Sound[] sounds)
    {
        _sounds = sounds;
    }

    public Sound getSound()
    {
        return _sounds[r.nextInt(_sounds.length)];
    }
}
