package obfuscate.comand;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;

import java.util.function.Function;

public enum CmdContext {
    CURRENT_GAME((player) -> MsdmPlugin.getGameServer().getGame(player))
    ;

    private final Function<StrikePlayer, Object> eval;

    CmdContext(Function<StrikePlayer, Object> eval) {
        this.eval = eval;
    }

    public Object evalForPlayer(WrappedSender sender) {
        if (sender.getPlayer() == null) {
            return null;
        }
        return eval.apply(sender.getPlayer());
    }
}
