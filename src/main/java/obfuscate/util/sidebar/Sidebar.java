package obfuscate.util.sidebar;

import obfuscate.MsdmPlugin;
import obfuscate.game.player.StrikePlayer;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;
import obfuscate.logging.Logger;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Sidebar {
    private final StrikePlayer player;
    private final List<String> lines = new ArrayList<>();
    ScoreboardObjective obj;
    Scoreboard sb;

    private final String objectiveId = "fake-sb";

    public Sidebar(final StrikePlayer player) {
        this.player = player;

        sb = new Scoreboard();
        obj = sb.registerObjective(
                objectiveId,
                IScoreboardCriteria.b,
                new ChatComponentText(""),
                IScoreboardCriteria.EnumScoreboardHealthDisplay.a
        );
    }

    private static void sendPacket(Player p, Packet packet){
        ((CraftPlayer)p).getHandle().b.sendPacket(packet);
    }

    public void create() {
        PacketPlayOutScoreboardObjective createPacket = new PacketPlayOutScoreboardObjective(obj, 0);
        PacketPlayOutScoreboardDisplayObjective display = new PacketPlayOutScoreboardDisplayObjective(1, obj);
        PacketPlayOutScoreboardObjective removePacket = new PacketPlayOutScoreboardObjective(obj, 1);

        sendPacket(player.getPlayer(), removePacket);
        sendPacket(player.getPlayer(), createPacket);
        sendPacket(player.getPlayer(), display);
    }

    public void destroy() {
        PacketPlayOutScoreboardObjective removePacket = new PacketPlayOutScoreboardObjective(obj, 1);
        sendPacket(player.getPlayer(), removePacket);
    }

    public void setTitle(String title) {
        obj.setDisplayName(new ChatComponentText(title));
        // 2 = update text
        PacketPlayOutScoreboardObjective p3 = new PacketPlayOutScoreboardObjective(obj, 2);
        sendPacket(player.getPlayer(), p3);
    }

    public void deleteLine(int line) {
        PacketPlayOutScoreboardScore pa1 = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.b, objectiveId, lines.get(line), line);
        sendPacket(player.getPlayer(), pa1);
        lines.set(line, "");
        sendPacket(player.getPlayer(), pa1);
    }

    public void setLine(int line, String text) {
        if (text.length() > 40) {
            text = text.substring(0, 40);
            Logger.warning("Sidebar text is too long! (Max 40 chars), got: '" + text + "'");
        }
        if (lines.size() > line) {
            deleteLine(line);
        }
        while (lines.size() <= line + 1) {
            lines.add("");
        }
        lines.set(line, text);
        PacketPlayOutScoreboardScore pa2 = new PacketPlayOutScoreboardScore(ScoreboardServer.Action.a, objectiveId, text, line);
        sendPacket(player.getPlayer(), pa2);
    }
}