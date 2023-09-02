package obfuscate.game.sound;

import obfuscate.game.core.Game;
import obfuscate.game.core.TeamGame;
import obfuscate.game.player.StrikePlayer;
import obfuscate.team.StrikeTeam;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

import javax.annotation.Nullable;

public class GameSoundManager {
    private final Game _game;

    public GameSoundManager(Game game) {
        _game = game;
    }

    public SoundFxBuilder tick() {
        return playSound(Sound.BLOCK_NOTE_BLOCK_HAT);
    }


    public class SoundFxBuilder {
        private Location at;
        private StrikeTeam forTeam;
        private StrikePlayer by;
        private StrikePlayer forPlayer;
        private @Nullable String soundKey;
        private @Nullable Sound sound;
        private float vol = 1F;
        private float pitch = 1F;

        public SoundFxBuilder(Sound s) {
            sound = s;
        }
        public SoundFxBuilder(String s) {
            soundKey = s;
        }

        public SoundFxBuilder at(Location at) {
            this.at = at;
            return this;
        }

        /** Broadcast sound directly into the ear of each player on team */
        public SoundFxBuilder forTeam(StrikeTeam forTeam) {
            this.forTeam = forTeam;
            return this;
        }

        public SoundFxBuilder forPlayer(StrikePlayer forPlayer) {
            this.forPlayer = forPlayer;
            return this;
        }

        public SoundFxBuilder by(StrikePlayer by) {
            this.by = by;
            return this;
        }

        public SoundFxBuilder vol(float v) {
            this.vol = v;
            return this;
        }
        public SoundFxBuilder soundDistance(int distanceBLocks) {
            this.vol = (distanceBLocks / 16f);
            return this;
        }

        public SoundFxBuilder pitch(float v) {
            this.pitch = v;
            return this;
        }

        public Location getAt() {
            return at;
        }

        public StrikeTeam getForTeam() {
            return forTeam;
        }

        public StrikePlayer getBy() {
            return by;
        }

        public StrikePlayer getForPlayer() {
            return forPlayer;
        }

        @Nullable
        public Sound getSound() {
            return sound;
        }

        @Nullable
        public String getSoundKey() {
            return soundKey;
        }

        private void playSound(StrikePlayer player, Location loc, float volume, float pitch) {
            if (getSound() != null) {
                player.getPlayer().playSound(loc, getSound(), volume, pitch);
            } else if (getSoundKey() != null) {
                player.getPlayer().playSound(loc, getSoundKey(), volume, pitch);
            }
        }

        private void playSound(World world, Location loc, float volume, float pitch) {
            if (getSound() != null) {
                world.playSound(loc, getSound(), volume, pitch);
            } else if (getSoundKey() != null) {
                world.playSound(loc, getSoundKey(), volume, pitch);
            }
        }

        public void play() {
            Location loc = null;

            if (getAt() != null) {
                loc = getAt();
            } else if (getBy() != null) {
                loc = getBy().getLocation();
            }

            if (getForTeam() != null) {
                TeamGame tGame = (TeamGame) _game;
                for (StrikePlayer player : _game.getOnline(tGame.getRoster(getForTeam()))) {

                    // make sure player is inside game and not chilling in hub
                    if (player.getGame() != _game) {
                        continue;
                    }

                    boolean wasNull = (loc == null);
                    if (wasNull) {
                        loc = player.getEyeLocation();
                    }

                    playSound(player, loc, vol, pitch);

                    if (wasNull) {
                        loc = null;
                    }

                }
            } else if (getForPlayer() != null) {
                if (loc == null) {
                    loc = getForPlayer().getEyeLocation();
                }
                playSound(getForPlayer(), loc, vol, pitch);
            } else {
                if (loc != null) {
                    playSound(_game.getTempMap().getWorld(), loc, vol, pitch);
                } else {
                    for (StrikePlayer player : _game.getOnlinePlayers()) {
                        loc = player.getEyeLocation();
                        playSound(_game.getTempMap().getWorld(), loc, vol, pitch);
                    }
                }
            }
        }
    }

    public SoundFxBuilder playSound(Sound s) {
        return new SoundFxBuilder(s);
    }
    public SoundFxBuilder playSound(String s) {
        return new SoundFxBuilder(s);
    }

    public SoundFxBuilder headshotThroughHelm() {
        return playSound(Sound.ENTITY_SPIDER_DEATH);
    }

    public SoundFxBuilder headshot() {
        return playSound(Sound.ENTITY_SPIDER_DEATH);
    }

    public SoundFxBuilder knifeStab() {
        return playSound(Sound.ENTITY_BAT_HURT);
    }

    public SoundFxBuilder knifeBackStab() {
        return playSound(Sound.ENTITY_IRON_GOLEM_DEATH);
    }

    public SoundFxBuilder tRoundStart() {
        return playSound(Radio.T_START.getSound());
    }

    public SoundFxBuilder ctRoundStart() {
        return playSound(Radio.CT_START.getSound());
    }

    public SoundFxBuilder dropBomb() {
        return playSound(Radio.T_BOMB_DROP.getSound());
    }

    public SoundFxBuilder bombPlanted() {
        return playSound(Radio.BOMB_PLANT.getSound());
    }

    public SoundFxBuilder harp() {
        return playSound(Sound.BLOCK_NOTE_BLOCK_HARP);
    }

    public SoundFxBuilder bombDefused() {
        return playSound(Radio.BOMB_DEFUSE.getSound());
    }

    public SoundFxBuilder bombDefuse() {
        return playSound(Sound.BLOCK_PISTON_EXTEND);
    }

    public SoundFxBuilder bombPlant() {
        return playSound(Radio.T_BOMB_PLANT.getSound());
    }

    public SoundFxBuilder fireStart() {
        return playSound(Sound.ENTITY_IRON_GOLEM_ATTACK);
    }

    public SoundFxBuilder fireExtinguish() {
        return playSound(Sound.BLOCK_LAVA_EXTINGUISH);
    }

    public SoundFxBuilder burn() {
        return playSound(Sound.ENTITY_PIG_DEATH);
    }

    public SoundFxBuilder breakGlass() {
        return playSound(Sound.BLOCK_GLASS_BREAK);
    }

    public SoundFxBuilder blockHit() {
        return playSound(Sound.ENTITY_ENDERMAN_HURT);
    }

    public SoundFxBuilder announceTWin() {
        return playSound(Radio.T_WIN.getSound());
    }

    public SoundFxBuilder announceCTWin() {
        return playSound(Radio.CT_WIN.getSound());
    }

    public SoundFxBuilder startReload() {
        return playSound(Sound.BLOCK_PISTON_EXTEND);
    }

    public SoundFxBuilder endReload() {
        return playSound(Sound.BLOCK_PISTON_CONTRACT);
    }

    public SoundFxBuilder bombExplode() {
        return playSound(Sound.BLOCK_ANVIL_LAND);
    }

    public SoundFxBuilder grenadeRebound() {
        return playSound(Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR);
    }

    public SoundFxBuilder bombTick() {
        return playSound(Sound.BLOCK_ANVIL_DESTROY);
    }

    public SoundFxBuilder ggBrassBell() {
        return playSound("betterms.gungame.brass_bell");
    }

    public SoundFxBuilder ggLevelUp() {
        return playSound("betterms.gungame.levelup");
    }

    public SoundFxBuilder ggLevelDown() {
        return playSound("betterms.gungame.leveldown");
    }

    public SoundFxBuilder ggKnifeLevel() {
        return playSound("betterms.gungame.knife_level");
    }

    public SoundFxBuilder ggLostLead() {
        return playSound("betterms.gungame.lostlead");
    }

    public SoundFxBuilder ggNadeLevel() {
        return playSound("betterms.gungame.nade_level");
    }

    public SoundFxBuilder ggTakenLead() {
        return playSound("betterms.gungame.taken_lead");
    }

    public SoundFxBuilder ggTiedLead() {
        return playSound("betterms.gungame.tied_lead");
    }

    public SoundFxBuilder ggTriple() {
        return playSound("betterms.gungame.triple");
    }

    public SoundFxBuilder ggWelcome() {
        return playSound("betterms.gungame.welcome");
    }

    public SoundFxBuilder ggWinner() {
        return playSound("betterms.gungame.winner");
    }

    public SoundFxBuilder bass() {
        return playSound(Sound.BLOCK_NOTE_BLOCK_BASS);
    }

}
