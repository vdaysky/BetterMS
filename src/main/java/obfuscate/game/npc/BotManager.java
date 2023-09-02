package obfuscate.game.npc;

import obfuscate.MsdmPlugin;
import obfuscate.game.core.Game;
import obfuscate.game.npc.trait.ReplayTrait;
import obfuscate.game.player.BotPlayer;
import obfuscate.game.player.StrikePlayer;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.HashMap;

public class BotManager
{
    private static HashMap<String, NPC> pathRunners = new HashMap<>();

    public static boolean isPathActive(String pathName)
    {
        return pathRunners.get(pathName) != null;
    }

    public static String getRandomBotName() {
        String[] options = {"A", "B", "C", "D", "E", "F", "G", "H"};
        return options[(int) (Math.random() * options.length)];
    }

    public static NPC createRunnerBot(Game game, PrerecordedPath path)
    {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, getRandomBotName());
        pathRunners.put(path.getPathName(), npc);

        npc.addTrait(new ReplayTrait(game, path));
        npc.setProtected(false);
        npc.setFlyable(false);

        npc.spawn(game.getTempMap().getWorld().getSpawnLocation());

        StrikePlayer botPlayer = StrikePlayer.getOrCreate(npc);
        botPlayer.getInitializationPromise().thenSync(
            x -> {
                game.tryJoinPlayer(botPlayer,false).thenSync((y)-> {
                    npc.getTraitNullable(ReplayTrait.class).onGameJoin();
                    return null;
                });
                return null;
            }
        );
        return npc;
    }

    public static BotPlayer createBot(String name, Trait ... traits) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.setProtected(false);
        npc.setFlyable(false);

        Arrays.stream(traits).forEach(npc::addTrait);
        npc.spawn(MsdmPlugin.getGameServer().getFallbackServer().getTempMap().getWorld().getSpawnLocation());

        return StrikePlayer.getOrCreate(npc);
    }

    public static void stopPath(String toStop)
    {
        NPC runner = pathRunners.get(toStop);

        runner.destroy();
    }
}
