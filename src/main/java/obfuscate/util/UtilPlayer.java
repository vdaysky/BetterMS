package obfuscate.util;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.BotPlayer;
import obfuscate.game.player.StrikePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UtilPlayer {
    public static HashMap<StrikePlayer, Double> getInRadius(Location loc, int dR)
    {
        HashMap<StrikePlayer, Double> players = new HashMap<>();

        for (Player cur : loc.getWorld().getPlayers())
        {
            if (cur.getGameMode() == GameMode.SPECTATOR)
                continue;

            double offset = UtilMath.offset(loc, cur.getLocation());

            if (offset < dR)
                players.put(StrikePlayer.getOrCreate(cur), 1 - (offset / dR));
        }

        return players;
    }

    public static List<StrikePlayer> getOnline(Iterable<? extends StrikePlayer> offline) {
        List<StrikePlayer> online = new ArrayList<>();
        for (StrikePlayer player : offline) {

            if (player instanceof BotPlayer) {
                online.add(player);
                continue;
            }

            if ( player.isOnline() ) {
                online.add(player);
            }
        }
        return online;
    }

    public static void hideAllExceptSameLobby(StrikePlayer forPlayer)
    {
//        if (forPlayer.isNPC()) return;

        for(Player pl : Bukkit.getOnlinePlayers())
        {
            StrikePlayer everyPlayer = StrikePlayer.getOrCreate(pl);

            if (MsdmPlugin.getGameServer().getGame(everyPlayer) != MsdmPlugin.getGameServer().getGame(forPlayer)) {
                UtilTeam.removePlayer(everyPlayer, forPlayer);
            }
            else {
                UtilTeam.addPlayer(everyPlayer, forPlayer);
            }
        }
    }
}
