package obfuscate.game.dataregistry;

import obfuscate.game.player.StrikePlayer;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ScopedDataRegistry {

    private final HashMap<StrikePlayer, HashMap<DataKey, Object>> playerRoundDataRegistry = new HashMap<>();
    private final HashMap<StrikePlayer, HashMap<DataKey, Object>> playerRoundLifeRegistry = new HashMap<>();

    private final HashMap<StrikePlayer, HashMap<DataKey, Object>> playerLifeRegistry = new HashMap<>();

    public void signal(ResetSignal signal, @Nullable StrikePlayer player) {
        if (signal == ResetSignal.ROUND_END) {
            playerRoundLifeRegistry.clear();
            playerRoundDataRegistry.clear();
        }
        if (signal == ResetSignal.PLAYER_DEATH) {
            if (playerRoundLifeRegistry.containsKey(player)) {
                playerRoundLifeRegistry.get(player).clear();
            }
        }
    }

    private HashMap<StrikePlayer, HashMap<DataKey, Object>> getScopeRegistry(DataScope scope) {
        if (scope == DataScope.ROUND) {
            return playerRoundDataRegistry;
        }
        if (scope == DataScope.LIFE) {
            return playerLifeRegistry;
        }
        if (scope == DataScope.ROUND_LIFE) {
            return playerRoundLifeRegistry;
        }
        return null;
    }

    public <T> T getForPlayer(StrikePlayer player, DataScope scope, DataKey key, T defaultVal) {

        var playerData = getScopeRegistry(scope).get(player);
        if (playerData == null) {
            return defaultVal;
        }
        if (!playerData.containsKey(key)) {
            return defaultVal;
        }

        return (T) playerData.get(key);
    }

    public int increment(StrikePlayer player, DataScope scope, DataKey key, int defaultVal) {
        int val = getForPlayer(player, scope, key, defaultVal);
        val++;
        setForPlayer(player, scope, key, val);
        return val;
    }

    public <T> void setForPlayer(StrikePlayer player, DataScope scope, DataKey key, T value) {

        var reg = getScopeRegistry(scope);
        if (!reg.containsKey(player)) {
            reg.put(player, new HashMap<>());
        }
        var playerData = reg.get(player);

        playerData.put(key, value);
    }
}
