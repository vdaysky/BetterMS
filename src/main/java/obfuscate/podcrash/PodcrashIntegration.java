package obfuscate.podcrash;

import obfuscate.game.player.StrikePlayer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.resources.MinecraftKey;


public class PodcrashIntegration {


    public static void setFeature(StrikePlayer player, ACFeature feature, boolean active) {

        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("feature", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeByte(active ? 1 : 0);
        buf.writeCharSequence(feature.getFeatureName(), java.nio.charset.StandardCharsets.UTF_8);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

    public static void setTabListMap(StrikePlayer player, String mapName) {
        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("game", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence("map", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(mapName, java.nio.charset.StandardCharsets.UTF_8);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

    public static void createTabVariable(StrikePlayer player, String varName) {
        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("game", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence("variablecreate", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(varName, java.nio.charset.StandardCharsets.UTF_8);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

    public static void createTeam(StrikePlayer player, String teamName, byte teamColor) {
        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("game", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence("teamcreate", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(teamName, java.nio.charset.StandardCharsets.UTF_8);
        buf.writeByte(teamColor);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

    public static void assignTeam(StrikePlayer player, String playerName, String teamName) {
        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("game", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence("assignteam", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(playerName, java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(teamName, java.nio.charset.StandardCharsets.UTF_8);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

    public static void setTeamScore(StrikePlayer player, String teamName, int score) {
        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("game", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence("teamscore", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(teamName, java.nio.charset.StandardCharsets.UTF_8);
        buf.writeInt(score);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

    public static void setTabVariable(StrikePlayer player, String playerName, String varName, String value) {
        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("game", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence("playerset", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(playerName, java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(varName, java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence(value, java.nio.charset.StandardCharsets.UTF_8);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

    public static void resetAll(StrikePlayer player) {
        ByteBuf buf = Unpooled.buffer(256);
        var ser = new PacketDataSerializer(buf);

        buf.writeCharSequence("game", java.nio.charset.StandardCharsets.UTF_8);
        buf.writeCharSequence("flush", java.nio.charset.StandardCharsets.UTF_8);

        PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(new MinecraftKey("podcrash:client"), ser);
        player.sendPacket(packet);
    }

}
