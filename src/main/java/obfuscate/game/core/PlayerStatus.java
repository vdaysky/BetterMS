package obfuscate.game.core;

import org.bukkit.GameMode;

public enum PlayerStatus {
    PARTICIPATING(GameMode.ADVENTURE),
    SPECTATING(GameMode.SPECTATOR),
    COACHING(GameMode.SPECTATOR),

    ;

    private final GameMode gameMode;

    PlayerStatus(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}
