package obfuscate.game.core.plugins;

import obfuscate.MsdmPlugin;
import obfuscate.event.LocalEvent;
import obfuscate.event.bukkit.BulletHitEvent;
import obfuscate.event.custom.config.ConfigFieldChangeEvent;
import obfuscate.event.custom.game.GameInitEvent;
import obfuscate.event.custom.player.PlayerJoinGameEvent;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.traits.ISharedContext;
import obfuscate.game.npc.trait.TargetPracticeTrait;
import obfuscate.game.player.BotPlayer;
import obfuscate.game.player.StrikePlayer;
import obfuscate.game.state.GameStateInstance;
import obfuscate.game.state.GeneralGameStage;
import obfuscate.game.state.StateTag;
import obfuscate.gamemode.Competitive;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.HashSet;

public class TargetPracticePlugin implements IPlugin<Competitive> {

    private Competitive instance;
    private HashSet<Location> targets = new HashSet<>();

    @Override
    public void preInit(Competitive instance) {
        this.instance = instance;
        instance.getConfig().setValue(instance, ConfigField.BUY_ANYWHERE, 1);
        instance.getConfig().setValue(instance, ConfigField.BUY_TIME, -1);
        instance.getConfig().setValue(instance, ConfigField.FREE_GUNS, 1);
        instance.getConfig().setValue(instance, ConfigField.ITEM_DESTROY_ON_DROP, 1);
        instance.getConfig().setValue(instance, ConfigField.LIVE_DURATION, 999);
        instance.getConfig().setValue(instance, ConfigField.INFINITE_AMMO, 1);
        instance.getConfig().setValue(instance, ConfigField.WINS_BY_ELIMINATION, 0);

        // humans CT, bots T
        instance.getConfig().setValue(instance, ConfigField.HUMAN_TEAM, 1);

        ISharedContext traits = instance.getSharedContext();

        traits.setGameStages(
            Arrays.asList(
                new GameStateInstance(
                    "Target Practice",
                    GeneralGameStage.LIVE,
                    StateTag.RESPAWNABLE,
                    StateTag.JOINABLE,
                    StateTag.TICKABLE,
                    StateTag.DAMAGE_ALLOWED,
                    StateTag.CAN_INTERACT,
                    StateTag.CAN_MOVE
                )
            )
        );

        instance.setLeftDuration(-1);
    }

    @LocalEvent
    private void onWorldLoaded(GameInitEvent e) {
        int count = instance.get(ConfigField.TARGET_COUNT);

        for (int i = 0; i < count; i++) {
            generateTarget();
        }
    }

    @LocalEvent
    private void onJoin(PlayerJoinGameEvent e) {
        if (!e.getPlayer().isBot()) {
            return;
        }
        BotPlayer bot = (BotPlayer) e.getPlayer();

        for (StrikePlayer player : instance.getOnlinePlayers()) {
            bot.getPlayer().showPlayer(player.getPlayer());
            player.getPlayer().showPlayer(bot.getPlayer());
        }

    }

    private void generateTarget() {
        int dist = instance.get(ConfigField.TARGET_DISTANCE);
        int width = instance.get(ConfigField.TARGET_WIDTH_SPREAD);
        int height = instance.get(ConfigField.TARGET_HEIGHT_SPREAD);
        int distance = instance.get(ConfigField.TARGET_DISTANCE_SPREAD);

        boolean smallTargets = instance.get(ConfigField.TARGET_SMALL) == 1;

        Location loc;

        do {
            World w = instance.getTempMap().getWorld();
            loc = new Location(w, 25, 22, 50);

            int dx = (int) (Math.random() * width) - width / 2;
            int dy = (int) (Math.random() * height);
            int dz = (int) (Math.random() * distance) + dist;

            loc.add(dx, dy, dz);
            if (smallTargets) {
                loc.getBlock().setType(Material.SKELETON_SKULL);
            } else {
                loc.getBlock().setType(Material.REDSTONE_BLOCK);
            }

        } while (loc.getBlock().getType() == Material.AIR);

        targets.add(loc);
    }

    @LocalEvent
    private void onNpcSetMoving(ConfigFieldChangeEvent e) {
        if (e.getField() == ConfigField.NPC_STRAFE) {
            boolean isMoving = e.getNewValue() == 1;
            var bots = instance.getBots();
            for (BotPlayer bot : bots) {
                if (!bot.getNPC().hasTrait(TargetPracticeTrait.class)) {
                    continue;
                }
                TargetPracticeTrait trait = bot.getNPC().getTrait(TargetPracticeTrait.class);
                trait.setMoving(isMoving);
            }
        }
        if (e.getField() == ConfigField.SHOW_HITBOX) {
            boolean show = e.getNewValue() == 1;
            var bots = instance.getBots();
            for (BotPlayer bot : bots) {
                if (!bot.getNPC().hasTrait(TargetPracticeTrait.class)) {
                    continue;
                }
                TargetPracticeTrait trait = bot.getNPC().getTrait(TargetPracticeTrait.class);
                trait.setShowHitbox(show);
            }
        }
    }

    @LocalEvent
    private void onHit(BulletHitEvent event) {
        Block hitBlock = event.getHitLocation().getBlock();

        if (targets.contains(hitBlock.getLocation())) {
            targets.remove(hitBlock.getLocation());
            hitBlock.setType(Material.AIR);
        } else {
            return;
        }

        int count = instance.get(ConfigField.TARGET_COUNT);

        event.getBullet().getShooter().playSound(Sound.BLOCK_NOTE_BLOCK_BANJO);

        if (targets.size() > count) {
            return;
        }

        while (targets.size() < count) {
            generateTarget();
        }
    }
}
