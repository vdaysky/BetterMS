package obfuscate.util;


import obfuscate.MsdmPlugin;
import obfuscate.game.config.ConfigField;
import obfuscate.game.core.Game;
import obfuscate.game.player.BotPlayer;
import obfuscate.game.player.StrikePlayer;
import obfuscate.team.StrikeTeam;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.scoreboard.*;

public class UtilTeam
{
    public static void addPlayer(StrikePlayer player, StrikePlayer addFor)
    {
        if (!addFor.isOnline())
            return;
        addFor.getPlayer().showPlayer(player.getPlayer());
        player.getPlayer().showPlayer(addFor.getPlayer());

//        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.getCraftPlayer());
//        addFor.sendPacket(packet);
    }

    public static void removePlayer(StrikePlayer player, StrikePlayer removeFor)
    {
        if (!removeFor.isOnline())
            return;

        removeFor.getPlayer().hidePlayer(player.getPlayer());
        player.getPlayer().hidePlayer(removeFor.getPlayer());
//        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, player.getCraftPlayer());
//        removeFor.sendPacket(packet);
    }

//    public static void hideNameTag(StrikePlayer player, StrikePlayer hideFor)
//    {
//        Player p = hideFor.getPlayer(); //Player to target
//
//        ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), p.getName());
//
//        team.setNameTagVisibility(ScoreboardTeamBase.EnumNameTagVisibility.NEVER);
//
//        ArrayList<String> playerToAdd = new ArrayList<>();
//
//        playerToAdd.add(player.getName()); //Add the fake player so this player will not have a nametag
//        System.out.println("Hide " + player.getName() + " for " + hideFor.getName());
//        hideFor.sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
//        hideFor.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
//        hideFor.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));
//    }

    public static Team getBukkitTeam(Scoreboard scoreboard, String name, boolean canSeeTeamNames)
    {
        if (!canSeeTeamNames) {
            name = "hidden_names";
        }

//        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team bukkitTeam = scoreboard.getTeam(name.toString());

        if (bukkitTeam == null) {
            bukkitTeam = scoreboard.registerNewTeam(name.toString());
        }

        bukkitTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

        if (!canSeeTeamNames) {
            bukkitTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        } else {
            bukkitTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
        }

        return bukkitTeam;
    }

    public static void join(StrikePlayer player, Scoreboard scoreboard, StrikeTeam team, String teamId, boolean canSeeTeamNames)
    {
        Team team_ = getBukkitTeam(scoreboard, teamId, canSeeTeamNames);
        team_.setColor(team.getChatColor());
        String entry;
        if (player.isNPC()) {
            BotPlayer bot = (BotPlayer) player;
            NPC npc = bot.getNPC();
            npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, "false");
            return;
        } else {
            entry = player.getActualName();
        }
        team_.addEntry(entry);
    }

    public static void leave(StrikePlayer player, Scoreboard scoreboard)
    {
        if (player.isNPC()) return;

        Game game = player.getGame();
        Team team_ = getBukkitTeam(scoreboard, "roster-" + game.getPlayerRoster(player).getTeam().name(), game.getConfig().getValue(ConfigField.CAN_SEE_TEAM_NAMES).bool());
        team_.removeEntry(player.getActualName());
    }
}
