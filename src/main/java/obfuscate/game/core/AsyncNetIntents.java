package obfuscate.game.core;

import obfuscate.MsdmPlugin;
import obfuscate.event.custom.damage.PlayerDeathEvent;
import obfuscate.event.custom.intent.ChangePlayerBlacklistStatusAtGameIntent;
import obfuscate.event.custom.intent.ChangePlayerWhitelistStatusAtGameIntent;
import obfuscate.event.custom.intent.PlayerJoinGameIntentEvent;
import obfuscate.event.custom.intent.PlayerLeaveGameIntentEvent;
import obfuscate.game.damage.DamageModifiers;
import obfuscate.game.damage.DamageReason;
import obfuscate.game.damage.DamageSourceWrapper;
import obfuscate.game.player.StrikePlayer;
import obfuscate.gamemode.Competitive;
import obfuscate.message.MsgSender;
import obfuscate.network.models.responses.IntentResponse;
import obfuscate.team.StrikeTeam;
import obfuscate.util.Promise;
import obfuscate.util.chat.C;
import org.bukkit.ChatColor;

/** Asynchronous ways to communicate intents  with backend and achieve different things */
public class AsyncNetIntents {


    private final IGame game;

    public AsyncNetIntents(IGame game) {
        this.game = game;
    }

    public Promise<? extends IntentResponse> tryJoinPlayer(StrikePlayer player, boolean spec, StrikeTeam team) {
        return new PlayerJoinGameIntentEvent(game, team, player, spec).trigger();
    }

    public Promise<? extends IntentResponse> tryLeavePlayer(StrikePlayer player) {
        return new PlayerLeaveGameIntentEvent(game, player).trigger();
    }

    public Promise<? extends IntentResponse> tryChangeWhitelistStatus(StrikePlayer player, boolean isWhitelisted) {
        return new ChangePlayerWhitelistStatusAtGameIntent(player, null, game, isWhitelisted).trigger();
    }

    public Promise<? extends IntentResponse> tryChangeBlacklistStatus(StrikePlayer target, boolean isBlacklisted, String reason) {
        return new ChangePlayerBlacklistStatusAtGameIntent(target, null, game, isBlacklisted, reason).trigger();
    }
}
