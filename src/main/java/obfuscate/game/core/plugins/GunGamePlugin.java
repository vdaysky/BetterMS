package obfuscate.game.core.plugins;

import obfuscate.event.LocalEvent;
import obfuscate.event.LocalPriority;
import obfuscate.event.custom.damage.PlayerDeathEvent;
import obfuscate.event.custom.gamestate.RoundTimerEndEvent;
import obfuscate.event.custom.gamestate.WarmUpEndEvent;
import obfuscate.event.custom.player.PlayerJoinGameEvent;
import obfuscate.event.custom.player.PlayerPreRespawnEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.traits.GunGameSidebarUpdater;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.sound.GameSoundManager;
import obfuscate.game.state.GameStateInstance;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.game.state.StateTag;
import obfuscate.gamemode.Competitive;
import obfuscate.mechanic.item.StrikeItem;
import obfuscate.mechanic.item.armor.Helmet;
import obfuscate.mechanic.item.armor.Kevlar;
import obfuscate.mechanic.item.guns.Gun;
import obfuscate.mechanic.item.guns.GunStats;
import obfuscate.mechanic.item.guns.Shotgun;
import obfuscate.mechanic.item.melee.Knife;
import obfuscate.mechanic.item.utility.grenade.HighExplosive;
import obfuscate.message.MsgSender;
import obfuscate.message.MsgType;
import obfuscate.util.time.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GunGamePlugin implements IPlugin<Competitive> {


    private Competitive instance;
    private final HashMap<StrikePlayer, Integer> levels = new HashMap<>();
    private final HashMap<StrikePlayer, Integer> levelKills = new HashMap<>();

    private ArrayList<StrikePlayer> leaders = new ArrayList<>();
    private int leaderScore = 1;

    private final StrikeItem[] weaponsByLevel = new StrikeItem[]{
        new Gun(GunStats.GLOCK_18),
        new Gun(GunStats.P2000),
        new Gun(GunStats.P250),
        new Gun(GunStats.DEAGLE),
        new Gun(GunStats.CZ75),
        new Gun(GunStats.PPBIZON),
        new Gun(GunStats.P90),
        new Shotgun(GunStats.NOVA),
        new Shotgun(GunStats.XM1014),
        new Gun(GunStats.GALIL),
        new Gun(GunStats.FAMAS),
        new Gun(GunStats.AK47),
        new Gun(GunStats.M4A4),
        new Gun(GunStats.SG553),
        new Gun(GunStats.AUG),
        new Gun(GunStats.SSG08),
        new Gun(GunStats.AWP),
        new HighExplosive(),
        new Knife()
    };
    private final Integer[] requiredKillsOnLevel = new Integer[]{
        2, // glock
        2, // p2000
        2, // p250
        2, // deagle
        2, // cz75
        2, // ppbizon
        2, // p90
        2, // nova
        2, // xm1014
        2, // galil
        2, // famas
        2, // ak47
        2, // m4a4
        2, // sg553
        2, // aug
        2, // ssg08
        2, // awp
        1, // he
        1  // knife
    };


    @Override
    public void preInit(Competitive instance) {
        this.instance = instance;
        instance.getSharedContext().setGameStages(
            List.of(
                new GameStateInstance("Live", GeneralGameStage.WARM_UP,
                        StateTag.TICKABLE,
                        StateTag.JOINABLE,
                        StateTag.CAN_MOVE,
                        StateTag.CAN_INTERACT,
                        StateTag.RESPAWNABLE,
                        StateTag.DAMAGE_ALLOWED
                )
            )
        );

        instance.getSharedContext().setSidebarUpdater(new GunGameSidebarUpdater(this));

        instance.getConfig().setValue(instance, ConfigField.RANDOM_SPAWNS, 1);
        instance.getConfig().setValue(instance, ConfigField.ALLOW_TEAM_DAMAGE, 1);
        instance.getConfig().setValue(instance, ConfigField.BUY_TIME, 0);
        instance.getConfig().setValue(instance, ConfigField.WINS_BY_ELIMINATION, 0);
        instance.getConfig().setValue(instance, ConfigField.ITEM_DESTROY_ON_DROP, 1);
        instance.getConfig().setValue(instance, ConfigField.ALLOW_ITEM_DROP, 0);
        instance.getConfig().setValue(instance, ConfigField.INFINITE_AMMO, 2);
        instance.getConfig().setValue(instance, ConfigField.CAN_SEE_TEAM_NAMES, 0);
        instance.getConfig().setValue(instance, ConfigField.KEEP_INVENTORY, 1);
    }

    @LocalEvent
    private void setLiveDuration(WarmUpEndEvent e) {
        e.getGame().setGameState(
            new GameStateInstance("Live", GeneralGameStage.WARM_UP,
                StateTag.TICKABLE,
                StateTag.JOINABLE,
                StateTag.CAN_MOVE,
                StateTag.CAN_INTERACT,
                StateTag.RESPAWNABLE,
                StateTag.DAMAGE_ALLOWED
            )
        );
        e.getGame().setLeftDuration(60 * 15);
    }

    public int getMaxLevel() {
        return weaponsByLevel.length;
    }

    public int getLevel(StrikePlayer player) {
        return levels.getOrDefault(player, 0);
    }

    public List<StrikePlayer> getLeaders() {
        return leaders;
    }

    public int getLeaderLevel() {
        return leaderScore;
    }

    public String getLevelName(int level) {
        return weaponsByLevel[level].getName();
    }

    private void endGame(StrikePlayer winner) {
        instance.getSoundManager().ggWinner().play();
        instance.endGame(
                instance.getTeamA(),
                instance.getTeamB()
        );

        if (winner != null) {
            instance.broadcast(
                    MsgSender.NONE,  winner.getName() + " Won the game!", MsgType.SUBTITLE
            );
        }
    }

    private void updateLeaders() {
        var newLeaderScore = Math.max(1, levels.values().stream().mapToInt(Integer::intValue).max().orElse(0));

        var newLeaders = new ArrayList<StrikePlayer>();
        var lostTheLead = new ArrayList<StrikePlayer>();

        for (var entry : levels.entrySet()) {
            if (entry.getValue() == newLeaderScore) {
                // player already was a leader
                newLeaders.add(entry.getKey());
            } else {
                // player is not a leader anymore
                if (leaders.contains(entry.getKey())) {
                    lostTheLead.add(entry.getKey());
                }
            }
        }

        if (newLeaders.size() == 1) {
            if (!leaders.contains(newLeaders.get(0))) {
                // announce new leader
                instance.getSoundManager().ggTakenLead().forPlayer(newLeaders.get(0)).play();
            } else {
                if (leaders.size() > 1) {
                    // announce taking lead from tie
                    instance.getSoundManager().ggTakenLead().forPlayer(newLeaders.get(0)).play();
                }
            }

        } else {
            // tied
            for (var player : newLeaders) {
                if (leaders.contains(player)) {
                    continue;
                }
                instance.getSoundManager().ggTiedLead().forPlayer(player).play();
            }
        }

        for (var player : lostTheLead) {
            instance.getSoundManager().ggLostLead().forPlayer(player).play();
        }

        leaders = newLeaders;
        leaderScore = newLeaderScore;
    }

    private void playAfterBell(GameSoundManager.SoundFxBuilder builder) {
        instance.getSoundManager().ggBrassBell().play();
        new Task(
                builder::play,
                10
        ).run();
    }

    private void setLevel(StrikePlayer player, Integer level) {
        levelKills.put(player, 0);
        levels.put(player, level);
        instance.getGameSession(player).getInventory().clear();
        var weapon = weaponsByLevel[level];
        weapon.copy().giveToPlayer(instance, player, true);
        new Knife().giveToPlayer(instance, player, true);
        new Kevlar().giveToPlayer(instance, player, true);
        new Helmet().giveToPlayer(instance, player, true);
    }

    private void decreaseLevel(StrikePlayer player) {
        var level = levels.getOrDefault(player, 0);
        int newLevel = Math.max(0, level - 1);
        setLevel(player, newLevel);
        instance.getSoundManager().ggLevelDown().forPlayer(player).play();
    }

    private void increaseLevel(StrikePlayer player) {
        var level = levels.getOrDefault(player, 0);

        int newLevel = level + 1;

        if (newLevel == 17) {
            playAfterBell(instance.getSoundManager().ggNadeLevel());
        }

        if (newLevel == 18) {
            playAfterBell(instance.getSoundManager().ggKnifeLevel());
        }

        if (newLevel >= weaponsByLevel.length) {
            // win game
            endGame(player);
            return;
        }
        setLevel(player, newLevel);
        instance.getSoundManager().ggLevelUp().forPlayer(player).play();
    }

    @LocalEvent
    private void onPlayerJoin(PlayerJoinGameEvent e) {

        new Task(
            instance.getSoundManager().ggWelcome().forPlayer(e.getPlayer())::play,
            60
        ).run();

        if (!levels.isEmpty()) {
            var averageLevel = levels.values().stream().mapToInt(Integer::intValue).sum() / levels.size();
            averageLevel = Math.max(0,averageLevel - 1);
            setLevel(e.getPlayer(), averageLevel);
        } else {
            setLevel(e.getPlayer(), 0);
        }
    }

    @LocalEvent
    private void onWarmupEndReset(WarmUpEndEvent e) {
        levels.clear();
        for (StrikePlayer player : instance.getOnlineDeadOrAliveParticipants()) {
            setLevel(player, 0);
        }
    }

    @LocalEvent(priority = LocalPriority.POST_HIGH)
    private void onRespawnSetGuns(PlayerPreRespawnEvent e) {
        setLevel(e.getPlayer(), levels.getOrDefault(e.getPlayer(), 0));
    }

    @LocalEvent
    private void onTimerEnd(RoundTimerEndEvent e) {
        if (leaders.isEmpty()) {
            endGame(null);
            return;
        }
        endGame(leaders.get(0));
    }

    @LocalEvent
    private void onKill(PlayerDeathEvent e) {

        StrikePlayer killer = e.getDamager();
        StrikePlayer killed = e.getDamagee();

        if (killer == killed) {
            decreaseLevel(killer);
            return;
        }

        if (killer == null) {
            decreaseLevel(killed);
            return;
        }

        var level = levels.getOrDefault(killer, 0);
        var requiredKills = requiredKillsOnLevel[level];
        var kills = levelKills.getOrDefault(killer, 0);
        var newKills = kills + 1;
        levelKills.put(killer, newKills);

        if (e.getDamageSource() instanceof Knife) {
            decreaseLevel(killed);
            increaseLevel(killer);
        } else if (newKills >= requiredKills) {
            increaseLevel(killer);
        }

        killer.partialHeal(0.25f, instance);
        updateLeaders();
    }

    public int getLevelKills(StrikePlayer player) {
        return levelKills.getOrDefault(player, 0);
    }

    public int levelKillRequirement(int level) {
        if (level >= requiredKillsOnLevel.length)
            return 0;

        return requiredKillsOnLevel[level];
    }

    public boolean isOnLastLevel(StrikePlayer player) {
        return levels.getOrDefault(player, 0) == weaponsByLevel.length - 1;
    }
}
